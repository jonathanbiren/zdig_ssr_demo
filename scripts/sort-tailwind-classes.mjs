#!/usr/bin/env node
/**
 * Sorts Tailwind classes in KTE templates using the official Tailwind
 * ordering (prettier-plugin-tailwindcss), resolved against the project's
 * real stylesheet (Tailwind v4 CSS-first config + daisyUI).
 *
 * Usage:
 *   node scripts/sort-tailwind-classes.mjs          # sort in place
 *   node scripts/sort-tailwind-classes.mjs --check  # report only, exit 1 if unsorted
 */
import { readdir, readFile, writeFile } from 'node:fs/promises'
import { join, relative } from 'node:path'
import { createSorter } from 'prettier-plugin-tailwindcss/sorter'

const root = process.cwd()
const stylesheetPath = 'src/main/resources/assets/styles.css'
const targetDirs = ['src/main/kte']
const extensions = ['.kte']

const checkMode = process.argv.includes('--check')

async function* walk(dir) {
  for (const entry of await readdir(dir, { withFileTypes: true })) {
    const full = join(dir, entry.name)
    if (entry.isDirectory()) yield* walk(full)
    else if (extensions.some((ext) => entry.name.endsWith(ext))) yield full
  }
}

const sorter = await createSorter({ base: root, stylesheetPath })

// Matches class="..." or class='...' attribute values.
const CLASS_ATTR = /\bclass\s*=\s*(?:"([^"]*)"|'([^']*)')/g

let changedFiles = 0
let sortedAttrs = 0

for (const dir of targetDirs) {
  for await (const file of walk(join(root, dir))) {
    const original = await readFile(file, 'utf8')
    let touched = false
    const updated = original.replace(CLASS_ATTR, (match, doubleQuoted, singleQuoted) => {
      const value = doubleQuoted ?? singleQuoted
      // Never touch values with template interpolation or directives.
      if (value.includes('${') || value.includes('@')) return match
      const [sorted] = sorter.sortClassAttributes([value])
      if (sorted !== value) {
        touched = true
        sortedAttrs++
      }
      return doubleQuoted != null ? `class="${sorted}"` : `class='${sorted}'`
    })
    if (touched) {
      changedFiles++
      const rel = relative(root, file)
      if (checkMode) {
        console.log(`unsorted: ${rel}`)
      } else {
        await writeFile(file, updated)
        console.log(`sorted:   ${rel}`)
      }
    }
  }
}

console.log(
  `${sortedAttrs} class attribute(s) ${checkMode ? 'to sort' : 'sorted'} in ${changedFiles} file(s)`,
)
if (checkMode && changedFiles > 0) process.exit(1)
