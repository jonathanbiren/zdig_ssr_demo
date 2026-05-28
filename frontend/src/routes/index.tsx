import {createFileRoute} from '@tanstack/solid-router'

export const Route = createFileRoute('/')({
    component: HomePage,
})

function HomePage() {


    return (
        <div>
            <div class=""><h1>This will be you navigation</h1></div>
            <div><h1>This will be your hero section</h1></div>
        </div>
    )
}
