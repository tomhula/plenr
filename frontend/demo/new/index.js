let randomInt = max => Math.floor(Math.random() * max)
let randomElement = arr => arr[randomIndex(arr)]
let randomIndex = arr => randomInt(arr.length)
let logistic = (x, L = 1, k = 1, x0 = 0) => L / (1 + Math.pow(Math.E, -k * (x - x0)))

const tasks = ["AcqAdc", "AcqMag", "BgFull", "BgNon", "Com", "Dis", "Diag", "Idle", "RteIni"]

const colors = [
    "#f48771", // Red-Orange
    "#7df693", // Lime Green
    "#7f96fd", // Royal Blue
    "#fb7bc5", // Hot Pink
    "#ffdd74", // Golden Yellow
    "#6ff3ea", // Aqua
    "#cl70fd", // Purple
    "#ffb072", // Orange
    "#74f3ce", // Mint Green
    "#649ef6", // Sky Blue
    "#f86ef8", // Magenta
    "#75bc75", // Neon Green
    "#fb5a5a" // Fire Engine Red
]

const series = []

for (let taskI = 0; taskI < tasks.length; taskI++)
    series [taskI] = {
        name: tasks [taskI],
        events: [],
        color: colors[taskI]
    }

const amountOfEvents = 100000
let time = randomInt(10)
for (let eventI = 0; eventI < amountOfEvents; eventI++)
{
    let serie = series [randomIndex(tasks)]
    const duration = randomInt(200)
    const quietDuration = randomInt(10)
    const start = time
    const end = start + duration
    const event = {start: start, end: end}
    time += duration + quietDuration
    serie.events.push(event)
}

const chartEle = document.querySelector("#chart")
let chart = new Chart(chartEle, series[0].events, {zoomFactor: 0.1, initialZoom: 1, minTimeTickDistancePixels: 100})

chart.draw()