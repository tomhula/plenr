let randomInt = max => Math.floor(Math.random() * max)
let randomElement = arr => arr[randomIndex(arr)]
let randomIndex = arr => randomInt(arr.length)

const chartEle = document.querySelector("#chart")
let chart = new Chart(chartEle, [], {zoomFactor: 0.1, initialZoom: 1, minTimeTickDistancePixels: 100})

const trainingEle = document.createElement("div")
trainingEle.innerText = "Training"
trainingEle.className = "training"

chart.draw()

chart.addEvent(trainingEle, new LocalTime(8, 0), 60)
