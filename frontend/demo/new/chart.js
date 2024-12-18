class Chart
{
    static #defaultOptions = {
        initialPan: 0,
        initialZoom: 0.1,
        zoomFactor: 0.1,
        wheelPanFactor: 0.5,
        minTimeTickDistancePixels: 200,
        maxZoom: 100,
        tooltip: {
            offsetX: 10,
            offsetY: 10
        },
        onEventClick: (eventEle, clickEvent) => {
        },
        onNewEvent: (event) => {
        }
    }

    chart = undefined
    #eventsRow = undefined

    #events = []

    #panState = {spaceDown: false, panning: false, lastX: 0, lastY: 0}
    #tooltip = undefined
    #timeAxis = undefined

    #chartAreaResizeObserver = undefined
    #chartAreaWidth = undefined
    #chartAreaIntersectionObserver = undefined
    #chartAreaLeft = undefined

    #shadowState = {dragging: false, dragStart: null, start: null, duration: null, element: null}

    constructor(chart, options)
    {
        this.chart = chart
        this.options = {...Chart.#defaultOptions, ...options}

        this.zoom = this.options.initialZoom
        this.pan = this.options.initialPan
    }

    draw()
    {
        clearElement(this.chart)

        // this.#initDebug()
        this.#initRows()
        this.#initChartAreaResizeObserver()
        this.#initTimeAxis()
        this.#handleUserPanZoomInputs()
        this.#updateEvents()
        // this.#createTooltip()
        this.#handleCreateNewEvent()
        /* Fixes the zoom if it is initially out of bounds */
        this.#zoom(0, this.#getChartAreaWidth() / 2)
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
            const mouseRelative = this.#getLocalXPos(e.clientX)
            const result = this.#localXPosToTime(mouseRelative)
            chartTimeMousePos.innerText = `Time: ${Math.floor(result)}min`
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

        this.zoom = clamp(this.zoom, this.#chartAreaWidth / 1440, this.options.maxZoom)

        /* Changing pan to center the zoom on origin */
        if (origin)
        {
            const totalOffset = (this.pan + origin)
            this.#addPan((totalOffset / previousZoom * this.zoom) - totalOffset)
        }
    }

    #setPan(pan)
    {
        this.pan = clamp(pan, 0, 1440 * this.zoom - this.#chartAreaWidth)
    }

    #addPan(pan)
    {
        this.#setPan(this.pan + pan)
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

    /** Updates event's element based on whether it is in view or not */
    #updateEvent(event)
    {
        const isInView = this.#isEventInView(event)
        let eventEle = event.__eventEle

        if (isInView)
        {
            let start = event.start
            const position = start * this.zoom - this.pan
            eventEle.style.width = event.duration * this.zoom + "px"
            eventEle.style.left = position + "px"
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
        const start = event.start
        const end = start + event.duration

        return this.#isTimeInView(start) || this.#isTimeInView(end)
            || (start < timeRangeInView.start && end > timeRangeInView.end)
    }

    #getChartAreaWidth()
    {
        if (this.#chartAreaWidth !== null && this.#chartAreaWidth !== undefined)
            return this.#chartAreaWidth

        const chartArea = this.chart
        this.#chartAreaWidth = chartArea.clientWidth
        return this.#chartAreaWidth
    }

    #handleUserPanZoomInputs()
    {
        document.addEventListener("keydown", e => {
            if (e.key === " " && !this.#panState.spaceDown)
            {
                e.preventDefault()
                this.#panState.spaceDown = true
                this.chart.style.cursor = "grab"
            }
        })
        document.addEventListener("keyup", e => {
            if (e.key === " " && this.#panState.spaceDown)
            {
                e.preventDefault()
                this.#panState.spaceDown = false
                if (this.#panState.panning)
                    this.chart.style.cursor = "grabbing"
                else
                     this.chart.style.removeProperty("cursor")
            }
        })

        this.chart.addEventListener("mousedown", e => {
            if (!e.target.classList.contains("events-row") && !e.target.classList.contains("event"))
                return
            if (!(e.button === 0 && this.#panState.spaceDown) && !(e.button === 1)) /* Space+LeftClick or MiddleClick */
                return

            e.preventDefault()
            this.chart.style.cursor = "grabbing"
            this.#panState = {...this.#panState, panning: true, lastX: e.clientX, lastY: e.clientY}
        })

        this.chart.addEventListener("mouseup", e => {
            if (!e.target.classList.contains("events-row") && !e.target.classList.contains("event"))
                return
            if (!(e.button in [0, 1])) // left or middle mouse button
                return

            e.preventDefault()
            if (this.#panState.spaceDown)
                this.chart.style.cursor = "grab"
            else
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
            this.#setPan(this.pan - distance)
            this.#updateEvents()
            this.#panState = {...this.#panState, lastX: e.clientX, lastY: e.clientY}
        })

        this.chart.addEventListener("wheel", e => {
            e.preventDefault()

            if (e.deltaY !== 0)
            {
                /* Zoom */
                if (e.ctrlKey)
                {
                    const rect = this.chart.getBoundingClientRect()
                    const origin = e.clientX - rect.x
                    this.#zoomNoUpdate(e.deltaY < 0 ? 1 : -1, origin)
                }
                /* Pan */
                else
                {
                    this.#addPan(e.deltaY * this.options.wheelPanFactor)
                }
            }

            /* Pan with horizontal wheel */
            if (e.deltaX !== 0)
                this.#addPan(e.deltaX * this.options.wheelPanFactor)

            this.#updateEvents()
        })
    }

    addEvent(eventEle, start, duration)
    {
        let event = {start: start.absoluteMinutes, duration: duration}
        eventEle.classList.add("event")
        event.__eventEle = eventEle
        eventEle.__event = event

        eventEle.addEventListener("click", e => {
            this.options.onEventClick(eventEle, e)
        })

        // eventEle.addEventListener("mouseover", e => {
        //     this.#tooltip.style.display = "block"
        //     this.#tooltip.innerText = (eventEle.end - eventEle.start) + "min"
        // })
        // eventEle.addEventListener("mousemove", e => {
        //     this.#tooltip.style.left = `${e.pageX + this.options.tooltip.offsetX}px`
        //     this.#tooltip.style.top = `${e.pageY + this.options.tooltip.offsetY}px`
        // })
        // eventEle.addEventListener("mouseout", e => {
        //     this.#tooltip.style.display = "none"
        // })
        this.#events.push(event)
        this.#eventsRow.appendChild(eventEle)
        this.#updateEvent(event)
    }

    #initChartAreaResizeObserver()
    {
        this.#chartAreaResizeObserver = new ResizeObserver(entries => {
            this.#chartAreaWidth = entries[0].contentRect.width
        })
        this.#chartAreaIntersectionObserver = new IntersectionObserver(entries => {
            this.#chartAreaLeft = entries[0].boundingClientRect.left
        })
        // const chartArea = this.chart.querySelector(".events-row")
        this.#chartAreaResizeObserver.observe(this.chart)
        this.#chartAreaIntersectionObserver.observe(this.chart)
    }

    #handleCreateNewEvent()
    {
        this.chart.addEventListener("mousemove", (event) => {
            if (!this.#shadowState.dragging)
                return
            const localXPos = this.#getLocalXPos(event.clientX)
            const time = this.#localXPosToTime(localXPos)
            if (time < this.#shadowState.dragStart)
            {
                this.#shadowState.start = time
                this.#shadowState.duration = this.#shadowState.dragStart - time
            }
            else if (time > this.#shadowState.dragStart)
            {
                this.#shadowState.start = this.#shadowState.dragStart
                this.#shadowState.duration = time - this.#shadowState.start
            }

            this.#updateShadow()
        })
        this.chart.addEventListener("mousedown", (event) => {
            if (event.button !== 0 || this.#panState.spaceDown )
                return
            event.preventDefault()
            this.#shadowState.dragging = true
            const time = this.#localXPosToTime(this.#getLocalXPos(event.clientX))
            this.#shadowState.start = time
            this.#shadowState.dragStart = time
            this.#shadowState.duration = 0
        })
        this.chart.addEventListener("mouseup", () => {
            this.#shadowState.dragging = false
            if (this.#shadowState.duration > 0)
            {
                const event = {start: this.#shadowState.start, duration: this.#shadowState.duration}
                this.options.onNewEvent(event)
            }
            this.#updateShadow()
        })
    }

    #localXPosToTime(localXPos)
    {
        return Math.round((this.pan + localXPos) / this.zoom)
    }

    #getLocalXPos(globalXPos)
    {
        return globalXPos - this.#chartAreaLeft
    }

    #updateShadow()
    {
        if (this.#shadowState.dragging && this.#shadowState.element === null)
        {
            this.#shadowState.element = newElement("div", "shadow")
            this.#eventsRow.appendChild(this.#shadowState.element)
        }
        else if (!this.#shadowState.dragging && this.#shadowState.element !== null)
        {
            this.#shadowState.element.remove()
            this.#shadowState.element = null
        }

        if (!this.#shadowState.dragging)
            return

        const start = this.#shadowState.start * this.zoom - this.pan
        const duration = this.#shadowState.duration
        this.#shadowState.element.style.left = start + "px"
        this.#shadowState.element.style.width = duration * this.zoom + "px"
    }
}