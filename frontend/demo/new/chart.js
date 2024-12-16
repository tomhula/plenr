class Chart
{
    static #defaultOptions = {
        initialPan: 0,
        initialZoom: 0.1,
        zoomFactor: 0.1,
        wheelPanFactor: 0.5,
        minTimeTickDistancePixels: 200,
        tooltip: {
            offsetX: 10,
            offsetY: 10
        },
        onClickEvent: () => {},
    }

    chart = undefined
    #eventsRow = undefined

    #elements = []
    #events = []

    #panState = {panning: false, lastX: 0, lastY: 0}
    #tooltip = undefined
    #timeAxis = undefined

    #chartAreaResizeObserver = undefined
    #chartAreaWidth = undefined

    constructor(chart, events, options)
    {
        this.chart = chart
        this.#events = events
        this.options = {...Chart.#defaultOptions, ...options}

        this.zoom = this.options.initialZoom
        this.pan = this.options.initialPan
    }

    draw()
    {
        clearElement(this.chart)

        this.#initDebug()
        this.#initRows()
        this.#initChartAreaResizeObserver()
        this.#initTimeAxis()
        this.#handleUserPanZoomInputs()
        this.#updateEvents()
        this.#createTooltip()
    }

    #initDebug()
    {
        let chartTimeMousePos = newElement("p")
        let chartViewRangeEle = newElement("p")
        let chartRelativeMousePos = newElement("p")
        let isMouseInView = newElement("p")
        let panEle = newElement("p")
        let zoomEle = newElement("p")
        document.body.appendChild(chartRelativeMousePos)
        document.body.appendChild(chartTimeMousePos)
        document.body.appendChild(isMouseInView)
        document.body.appendChild(chartViewRangeEle)
        document.body.appendChild(panEle)
        document.body.appendChild(zoomEle)

        this.chart.addEventListener("mousemove", e => {
            const chartArea = this.chart
            const mouseRelative = e.clientX - chartArea.getBoundingClientRect().x
            const result = (this.pan + mouseRelative) / this.zoom
            chartTimeMousePos.innerText = `Time: ${Math.floor(result)}µs`
            chartRelativeMousePos.innerText = `Chart relative pixels: ${Math.floor(mouseRelative)}px`
            isMouseInView.innerText = `Mouse in view: ${this.#isTimeInView(result)}`
            const chartViewRange = this.#getTimeRangeInView()
            const viewStart = Math.floor(chartViewRange.start)
            const viewEnd = Math.floor(chartViewRange.end)
            const viewDurMinutes = (viewEnd - viewStart)
            chartViewRangeEle.innerText = `View: ${viewStart}:${viewEnd} (${viewDurMinutes}min)`
            panEle.innerText = `Pan: ${Math.floor(this.pan)}px`
            zoomEle.innerText = `Zoom: ${this.zoom.toFixed(4)}`
        })
    }

    #initRows()
    {
        this.#eventsRow = newElement("div", "events-row")
        this.chart.appendChild(this.#eventsRow)
    }

    #createTooltip()
    {
        this.#tooltip = newElement("div", "tooltip")
        this.#tooltip.style.display = "hidden"
        this.chart.appendChild(this.#tooltip)
    }

    #zoomNoUpdate(amount, origin)
    {
        const previousZoom = this.zoom
        const zoomFactor = this.options.zoomFactor

        if (amount > 0)
            this.zoom *= 1 + zoomFactor * amount
        else
            this.zoom /= 1 + zoomFactor * -amount

        /* Changing pan to center the zoom on origin */
        if (origin)
        {
            const totalOffset = (this.pan + origin)
            this.pan += (totalOffset / previousZoom * this.zoom) - totalOffset
        }
    }

    #zoom(amount, origin)
    {
        const previousPan = this.pan
        const previousZoom = this.zoom

        this.#zoomNoUpdate(amount, origin)

        if (this.zoom !== previousZoom || this.pan !== previousPan)
            this.#updateEvents()
    }



    #updateTimeAxis()
    {
        clearElement(this.#timeAxis)

        const pixelInterval = this.options.minTimeTickDistancePixels
        const timeRangeInView = this.#getTimeRangeInView()
        const maxTicks = Math.floor(this.#getChartAreaWidth() / pixelInterval)
        const timeTickValues = this.#generateReadableTicks([timeRangeInView.start, timeRangeInView.end], maxTicks)

        for (const timeTickValue of timeTickValues)
        {
            const tickEle = newElement("div", "time-tick")
            const tickLineEle = newElement("div", "time-tick-line")
            const tickLabelEle = newElement("span", "time-tick-label")

            tickEle.style.left = (timeTickValue * this.zoom) - this.pan + "px"

            tickLineEle.style.top = -this.chart.clientHeight + "px"
            tickLineEle.style.height = this.chart.clientHeight + "px"

            tickLabelEle.innerText = new LocalTime(0, timeTickValue).toString()
            tickEle.appendChild(tickLineEle)
            tickEle.appendChild(tickLabelEle)
            this.#timeAxis.appendChild(tickEle)
            const tickLabelEleWidth = tickLabelEle.getBoundingClientRect().width
            tickLabelEle.style.left = -(tickLabelEleWidth / 2) + "px"
        }
    }

    /* Generated by ChatGPT */
    /* https://chatgpt.com/share/2c25dfe0-9125-4f28-9414-94ccc101f257 */
    /* https://chatgpt.com/share/97b84749-d6c7-4815-8a66-c833e299dc83 */
    #generateReadableTicks(timeRange, maxTicks)
    {
        const [start, end] = timeRange
        const rangeLength = end - start

        if (rangeLength <= 0 || maxTicks <= 0)
            return []

        // Calculate an approximate step size based on the desired number of ticks
        let roughStep = Math.ceil(rangeLength / maxTicks)

        // Define base intervals
        const bases = [1, 2, 5, 10]

        // Find the power of 10 of the roughStep
        const power = Math.floor(Math.log10(roughStep))
        const baseStep = Math.pow(10, power)

        // Determine the closest larger interval using the base intervals
        let stepSize = bases.find(base => baseStep * base >= roughStep) * baseStep

        // Ensure stepSize is at least 1
        stepSize = Math.max(stepSize, 1)

        // Generate ticks
        const ticks = []
        let currentTick = Math.floor(start / stepSize) * stepSize  // Start at the nearest step size multiple

        while (currentTick <= end)
        {
            if (currentTick >= start)
                ticks.push(currentTick)

            currentTick += stepSize
        }

        // If only one tick fits, ensure it returns that single tick
        if (ticks.length === 0)
            ticks.push(start)
        else if (ticks.length > maxTicks)
            // Adjust ticks to ensure maxTicks limit
            ticks.length = maxTicks

        return ticks
    }

    #initTimeAxis()
    {
        this.#timeAxis = newElement("div", "time-axis")
        this.chart.appendChild(this.#timeAxis)
    }

    #updateEvents()
    {
        /* Had sophisticated logic on how to update only events that are in view. */
        /* That logic was removed for readability, since there won't be many events in my use case */
        /* So performance is not an issue */

        for (const event of this.#events)
            this.#updateEvent(event)

        this.#updateTimeAxis()
    }

    /** Updates/creates/removes event's element based on whether it is in view or not */
    #updateEvent(event)
    {
        const isInView = this.#isEventInView(event)
        let eventEle = event.__eventEle

        if (isInView)
        {
            if (event.__eventEle === undefined)
            {
                eventEle = this.#newEventEle(event)
                this.#eventsRow.appendChild(eventEle)
            }

            const position = event.start * this.zoom - this.pan
            eventEle.style.transform = `translateX(${position}px) scaleX(${this.zoom})`

            /* Old positioning using absolute position */
            /*const width = timeDuration * this.zoom
            eventEle.style.left = position + "px"
            eventEle.style.width = width + "px"*/
        }
        else
        {
            if (eventEle !== undefined)
            {
                eventEle.remove()
                delete event.__eventEle
            }
        }

        return isInView
    }

    #isTimeInView(time)
    {
        const {start, end} = this.#getTimeRangeInView()
        return time >= start && time <= end
    }

    #getTimeRangeInView()
    {
        return {
            start: this.pan / this.zoom,
            end: (this.pan + this.#getChartAreaWidth()) / this.zoom
        }
    }

    #isEventInView(event)
    {
        const timeRangeInView = this.#getTimeRangeInView()

        return this.#isTimeInView(event.start) || this.#isTimeInView(event.end)
            || (event.start < timeRangeInView.start && event.end > timeRangeInView.end)
    }

    #getChartAreaWidth()
    {
        if (this.#chartAreaWidth !== null && this.#chartAreaWidth !== undefined)
            return this.#chartAreaWidth

        const chartArea = this.chart
        this.#chartAreaWidth = chartArea.clientWidth
        return this.#chartAreaWidth
    }

    #newEventEle(event)
    {
        const eventEle = newElement("div", "event")
        /* Set here, since we then scale the element using transforms */
        const timeDuration = event.end - event.start
        eventEle.style.width = timeDuration + "px"
        eventEle.style.backgroundColor = "red"

        eventEle.__event = event
        event.__eventEle = eventEle

        eventEle.addEventListener("mouseover", e => {
            this.#tooltip.style.display = "block"
            this.#tooltip.innerText = (event.end - event.start) + "min"
        })
        eventEle.addEventListener("mousemove", e => {
            this.#tooltip.style.left = `${e.pageX + this.options.tooltip.offsetX}px`
            this.#tooltip.style.top = `${e.pageY + this.options.tooltip.offsetY}px`
        })
        eventEle.addEventListener("mouseout", e => {
            this.#tooltip.style.display = "none"
        })

        return eventEle
    }

    #handleUserPanZoomInputs()
    {
        this.chart.addEventListener("mousedown", e => {
            if (!e.target.classList.contains("events-row") && !e.target.classList.contains("event"))
                return
            if (!(e.button in [0, 1])) // left or middle mouse button
                return

            e.preventDefault()
            this.chart.style.cursor = "grabbing"
            this.#panState = {panning: true, lastX: e.clientX, lastY: e.clientY}
        })

        this.chart.addEventListener("mouseup", e => {
            if (!e.target.classList.contains("events-row") && !e.target.classList.contains("event"))
                return
            if (!(e.button in [0, 1])) // left or middle mouse button
                return

            e.preventDefault()
            this.chart.style.removeProperty("cursor")
            this.#panState.panning = false
        })

        this.chart.addEventListener("mousemove", e => {
            if (!e.target.classList.contains("events-row") && !e.target.classList.contains("event"))
                return
            if (!this.#panState.panning)
                return

            if ((e.buttons & 1) === 0 && (e.buttons & 4) === 0) // left or middle mouse button
            {
                this.#panState.panning = false
                this.chart.style.removeProperty("cursor")
                return
            }

            e.preventDefault()
            const distance = e.clientX - this.#panState.lastX
            this.pan = this.pan - distance
            this.#updateEvents()
            this.#panState = {...this.#panState, lastX: e.clientX, lastY: e.clientY}
        })

        this.chart.addEventListener("wheel", e => {
            e.preventDefault()

            if (e.deltaY !== 0)
            {
                /* Pan */
                if (e.shiftKey)
                {
                    this.pan += e.deltaY * this.options.wheelPanFactor
                }
                /* Zoom */
                else
                {
                    const rect = this.chart.getBoundingClientRect()
                    const origin = e.clientX - rect.x
                    this.#zoomNoUpdate(e.deltaY < 0 ? 1 : -1, origin)
                }
            }

            /* Pan with horizontal wheel */
            if (e.deltaX !== 0)
                this.pan += e.deltaX * this.options.wheelPanFactor

            this.#updateEvents()
        })
    }

    #addEvent(event)
    {
        this.#events.push(event)
        this.#updateEvent(event)
    }

    #initChartAreaResizeObserver()
    {
        this.#chartAreaResizeObserver = new ResizeObserver(entries => {
            this.#chartAreaWidth = entries[0].contentRect.width
        })
        // const chartArea = this.chart.querySelector(".events-row")
        this.#chartAreaResizeObserver.observe(this.chart)
    }
}