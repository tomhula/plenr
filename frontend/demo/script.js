users = [
    {name: "Martina Nováková", remainingTrainings: 3, rangesAvailable: []},
    {name: "Jana Králová", remainingTrainings: 1},
    {name: "Petr Ježil", remainingTrainings: 2}
]


function createUserEle(user)
{
    let userEle = document.createElement("div")
    userEle.className = "user"
    let nameEle = document.createElement("span")
    nameEle.className = "name"
    nameEle.innerText = user.name
    let remainingTrainingsEle = document.createElement("span")
    remainingTrainingsEle.className = "remaining-trainings"
    remainingTrainingsEle.innerText = user.remainingTrainings
    userEle.appendChild(nameEle)
    userEle.appendChild(remainingTrainingsEle)
    return userEle
}

class LocalTime
{
    constructor(hours = 0, minutes = 0)
    {
        this.hours = hours
        this.minutes = minutes
        this.normalize()
    }

    normalize()
    {
        if (this.minutes >= 60)
        {
            this.hours += Math.floor(this.minutes / 60)
            this.minutes %= 60
        }
        if (this.minutes < 0)
        {
            const borrowedHours = Math.ceil(Math.abs(this.minutes) / 60)
            this.hours -= borrowedHours
            this.minutes += borrowedHours * 60
        }
        this.hours = (this.hours + 24) % 24
    }

    add(hours, minutes)
    {
        this.hours += hours
        this.minutes += minutes
        this.normalize()
    }

    subtract(hours, minutes)
    {
        this.hours -= hours
        this.minutes -= minutes
        this.normalize()
    }

    toString()
    {
        const formattedHours = String(this.hours).padStart(2, "0")
        const formattedMinutes = String(this.minutes).padStart(2, "0")
        return `${formattedHours}:${formattedMinutes}`
    }

    static fromString(timeString)
    {
        const [hours, minutes] = timeString.split(":").map(Number)
        return new LocalTime(hours, minutes)
    }
}

class Timeline
{
    element
    #width
    #left
    #hourWidth
    #minuteWidth
    #shadowEle
    #shadowState

    constructor(element)
    {
        this.element = element
        this.#width = element.getBoundingClientRect().width
        this.#hourWidth = this.#width / 24
        this.#minuteWidth = this.#hourWidth / 60
        new ResizeObserver((entries) => {
            this.#width = entries[0].contentRect.width
            this.#left = entries[0].contentRect.left
            this.#hourWidth = this.#width / 24
            this.#minuteWidth = this.#hourWidth / 60
        }).observe(element)
        this.#init()
    }

    #getNearestLowerHour(localXPos)
    {
        return this.#posToTime(localXPos).hours
    }

    #posToTime(xPos)
    {
        const hours = Math.floor(xPos / this.#hourWidth)
        const minutes = Math.floor((xPos % this.#hourWidth) / this.#minuteWidth)
        return new LocalTime(hours, minutes)
    }

    #init()
    {
        for (let i = 0; i < 24; i++)
        {
            let timeTickEle = this.#createTimeTickEle(i, 0)
            timeTickEle.style.left = `${i * 100 / 24}%`
            this.element.appendChild(timeTickEle)
        }

        this.#initShadow()
    }

    #initShadow()
    {
        this.#shadowState = {
            shown: false,
            dragging: false,
            start: LocalTime,
            end: LocalTime
        }
        this.#shadowEle = document.createElement("div")
        this.#shadowEle.className = "shadow"
        this.element.addEventListener("mouseenter", () => {
            this.#shadowState.shown = true
            this.#updateShadow()
        })
        this.element.addEventListener("mousemove", (event) => {
            const localXPos = this.#getLocalXPos(event.clientX)
            if (this.#shadowState.dragging === false)
            {
                const nearestLowerHour = this.#getNearestLowerHour(localXPos)
                this.#shadowState.start = new LocalTime(nearestLowerHour, 0)
                this.#shadowState.end = new LocalTime(nearestLowerHour + 1, 0)
            }
            else
            {
                this.#shadowState.end = new LocalTime(this.#getNearestLowerHour(localXPos) + 1, 0)
            }
            this.#updateShadow()
        })
        this.element.addEventListener("mousedown", (event) => {
            event.preventDefault()
            // this.#shadowState.dragging = !this.#shadowState.dragging
            this.#shadowState.dragging = true
            //this.#shadowState.start = this.#getNearestLowerHour(this.#getLocalXPos(event.clientX))
        })
        // mouse up
        this.element.addEventListener("mouseup", () => {
            this.#shadowState.dragging = false
            window.alert(`New training ${this.#shadowState.start}-${this.#shadowState.end}`)
        })
        this.element.addEventListener("mouseleave", () => {
            this.#shadowState.shown = false
            this.#updateShadow()
        })

        this.element.appendChild(this.#shadowEle)
    }

    #getLocalXPos(clientX)
    {
        return clientX - this.#left
    }

    #updateShadow()
    {
        this.#shadowEle.style.display = this.#shadowState.shown ? "block" : "none"
        const start = this.#shadowState.start
        const end = this.#shadowState.end
        const pos = (start.hours*60 + start.minutes) / (24*60)
        const width = (end.hours*60 + end.minutes) / (24*60) - pos
        this.#shadowEle.style.left = `${pos*100}%`
        this.#shadowEle.style.width = `${width*100}%`
    }

    #createTimeTickEle(hours, minutes)
    {
        const timeTickEle = document.createElement("div")
        timeTickEle.className = "time-tick"

        const tickEle = document.createElement("div")
        tickEle.className = "tick"

        const timeEle = document.createElement("span")
        timeEle.className = "time"
        timeEle.textContent = hours.toString().padStart(2, "0")
        const minutesEle = document.createElement("sup")
        minutesEle.textContent = minutes.toString().padStart(2, "0")
        timeEle.appendChild(minutesEle)

        timeTickEle.appendChild(tickEle)
        timeTickEle.appendChild(timeEle)

        return timeTickEle
    }

    addElementToTimeline(element, start, end)
    {
        const pos = (start.hours*60 + start.minutes) / (24*60)
        const width = ((end.hours*60 + end.minutes) - (start.hours*60 + start.minutes)) / (24*60)
        element.style.position = "absolute"
        element.style.left = `${pos*100}%`
        element.style.width = `${width*100}%`
        this.element.appendChild(element)
    }
}

function createTrainingEle(training)
{
    const participants = training.participants

    const trainingEle = document.createElement("div")
    trainingEle.className = "training"
    trainingEle.title = training.title + "\n" + training.start + "-" + training.end

    for (const [i, participant] of participants.entries())
    {
        const participantEle = document.createElement("div")
        participantEle.className = "participant"
        if (i === 0)
            participantEle.classList.add("first")
        else if (i === participants.length - 1)
            participantEle.classList.add("last")
        else
            participantEle.classList.add("middle")
        participantEle.innerText = participant.name
        trainingEle.appendChild(participantEle)
    }

    return trainingEle
}

let timelineEle = document.querySelector(".timeline")
let timeline = new Timeline(timelineEle)

const training = {
    title: "Trénink",
    participants: users,
    start: new LocalTime(8, 0),
    end: new LocalTime(10, 0)
}

let trainingELe = createTrainingEle(training)
timeline.addElementToTimeline(trainingELe, training.start, training.end)