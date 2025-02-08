let randomInt = max => Math.floor(Math.random() * max)
let randomElement = arr => arr[randomIndex(arr)]
let randomIndex = arr => randomInt(arr.length)

const chartEle = document.querySelector("#chart")
let options = {
    zoomFactor: 0.1,
    initialZoom: 1,
    minTimeTickDistancePixels: 100,
    onEventClick: (eventEle, clickEvent) => { console.log(eventEle.innerText) },
}
let chart = new Chart(chartEle, options)

const trainingEle = document.createElement("div")
trainingEle.innerText = "Training"
trainingEle.className = "training"

chart.draw()

chart.addEvent(trainingEle, new LocalTime(8, 0), 60)
