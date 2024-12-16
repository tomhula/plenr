class LocalTime
{
    constructor(hours = 0, minutes = 0)
    {
        this.hours = hours
        this.minutes = minutes
        this.normalize()
    }

    get absoluteMinutes()
    {
        return this.hours * 60 + this.minutes
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

function newElement(tag, clazz)
{
    let element = document.createElement(tag)
    if (clazz)
        element.className = clazz
    return element
}

/** Removes all children of the element */
function clearElement(element)
{
    while (element.firstChild)
        element.removeChild(element.firstChild)
}

/** @return min if number is smaller than min, else number */
function clampMin(number, min)
{
    return number < min ? min : number
}

function foreachIndexed(arr, fun)
{
    for (let i = 0; i < arr.length; i++)
    {
        const element = arr[i]
        fun(i, element)
    }
}

function foreach(arr, fun)
{
    for (const element of arr)
        fun(element)
}

function getKeyByValue(object, value)
{
    return Object.keys(object).find(key => object[key] === value)
}