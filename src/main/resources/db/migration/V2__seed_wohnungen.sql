INSERT INTO wohnungen (id, titel, beschreibung, strasse, hausnummer, postleitzahl, stadt, quadratmeter, zimmeranzahl, mietpreis_cent, status, erstellt_am) VALUES
(
    '11111111-1111-1111-1111-111111111111',
    'Helle 2-Zimmer-Wohnung in Stadtmitte',
    'Schöne, vollständig renovierte Wohnung in zentraler Lage. Die Wohnung verfügt über ein modernes Badezimmer, eine vollausgestattete Küche und großen Balkon mit Stadtblick. Ideal für Einzelpersonen oder Paare.',
    'Hauptstraße', '12', '53111', 'Bonn',
    62.50, 2.0, 85000, 'VEROEFFENTLICHT', now() - INTERVAL '10 days'
),
(
    '22222222-2222-2222-2222-222222222222',
    'Geräumige 3-Zimmer-Wohnung mit Garten',
    'Familienfreundliche Erdgeschosswohnung mit eigenem Gartenzugang. Ruhige Lage in einem gepflegten Mehrfamilienhaus, nur 10 Minuten vom Kasernengelände entfernt. Kellerabteil und Stellplatz inklusive.',
    'Gartenweg', '5', '53177', 'Bonn',
    89.00, 3.0, 110000, 'VEROEFFENTLICHT', now() - INTERVAL '8 days'
),
(
    '33333333-3333-3333-3333-333333333333',
    'Modernes Studio nahe Bundeswehr-Kaserne',
    'Kompaktes aber sehr funktionales Studio-Apartment, ideal für Soldaten im Dienst. Vollmöbliert, WLAN inklusive. Direkter Busanschluss zur Kaserne. Kurzfristig verfügbar.',
    'Kasernenallee', '88', '80333', 'München',
    32.00, 1.0, 72000, 'VEROEFFENTLICHT', now() - INTERVAL '5 days'
),
(
    '44444444-4444-4444-4444-444444444444',
    '4-Zimmer-Wohnung für Familien',
    'Großzügige Familienwohnung im dritten Obergeschoss mit Aufzug. Zwei Kinderzimmer, großes Wohnzimmer und separate Küche. Ruhiges Wohnviertel mit Schule und Kindergarten in der Nähe.',
    'Birkenweg', '3', '22083', 'Hamburg',
    105.00, 4.0, 148000, 'VEROEFFENTLICHT', now() - INTERVAL '3 days'
),
(
    '55555555-5555-5555-5555-555555555555',
    'Möbliertes Zimmer in WG',
    'Ruhiges möbliertes Zimmer in einer 4er-WG. Gemeinschaftsküche und Bad vorhanden. Internetzugang inklusive. Perfekt für kurze Stationierungen oder Übergangszeiten.',
    'Studentenstraße', '21', '80335', 'München',
    18.00, 1.0, 45000, 'VEROEFFENTLICHT', now() - INTERVAL '2 days'
),
(
    '66666666-6666-6666-6666-666666666666',
    'Penthouse mit Dachterrasse',
    'Exklusives Penthouse im letzten Stockwerk mit 40qm Dachterrasse und Panoramablick. Gehobene Ausstattung, offene Küche, zwei Schlafzimmer. Tiefgaragenstellplatz inklusive.',
    'Am Rheinufer', '1', '53113', 'Bonn',
    120.00, 3.0, 195000, 'VEROEFFENTLICHT', now() - INTERVAL '1 day'
),
(
    '77777777-7777-7777-7777-777777777777',
    'Günstige 1-Zimmer-Wohnung',
    'Einfache aber saubere 1-Zimmer-Wohnung, günstig und zentral gelegen. Badezimmer mit Dusche, Pantryküche. Für längere Aufenthalte geeignet.',
    'Nebenstraße', '44b', '22305', 'Hamburg',
    28.00, 1.0, 55000, 'ENTWURF', now() - INTERVAL '1 day'
);
