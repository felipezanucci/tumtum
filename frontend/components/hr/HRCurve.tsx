'use client'

import { useEffect, useRef, useState } from 'react'
import * as d3 from 'd3'
import type { Peak, TimelineEntry } from '@/lib/api'

interface HRDataPoint {
  time: string
  bpm: number
}

interface HRCurveProps {
  data: HRDataPoint[]
  peaks?: Peak[]
  timeline?: TimelineEntry[]
  width?: number
  height?: number
  animated?: boolean
  className?: string
}

export default function HRCurve({
  data,
  peaks = [],
  timeline = [],
  width = 800,
  height = 300,
  animated = true,
  className = '',
}: HRCurveProps) {
  const svgRef = useRef<SVGSVGElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const [dimensions, setDimensions] = useState({ width, height })

  // Responsive resize
  useEffect(() => {
    if (!containerRef.current) return

    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        const { width: w } = entry.contentRect
        if (w > 0) setDimensions({ width: w, height })
      }
    })

    observer.observe(containerRef.current)
    return () => observer.disconnect()
  }, [height])

  // D3 rendering
  useEffect(() => {
    if (!svgRef.current || data.length === 0) return

    const svg = d3.select(svgRef.current)
    svg.selectAll('*').remove()

    const margin = { top: 20, right: 20, bottom: 40, left: 50 }
    const w = dimensions.width - margin.left - margin.right
    const h = dimensions.height - margin.top - margin.bottom

    const g = svg
      .attr('width', dimensions.width)
      .attr('height', dimensions.height)
      .append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`)

    // Parse data
    const parsed = data.map((d) => ({
      time: new Date(d.time),
      bpm: d.bpm,
    }))

    // Scales
    const xScale = d3
      .scaleTime()
      .domain(d3.extent(parsed, (d) => d.time) as [Date, Date])
      .range([0, w])

    const yScale = d3
      .scaleLinear()
      .domain([
        Math.max(30, (d3.min(parsed, (d) => d.bpm) || 60) - 10),
        Math.min(220, (d3.max(parsed, (d) => d.bpm) || 120) + 10),
      ])
      .range([h, 0])

    // Gradient fill
    const gradientId = 'hr-gradient'
    const defs = svg.append('defs')
    const gradient = defs
      .append('linearGradient')
      .attr('id', gradientId)
      .attr('x1', '0%')
      .attr('y1', '0%')
      .attr('x2', '0%')
      .attr('y2', '100%')
    gradient.append('stop').attr('offset', '0%').attr('stop-color', '#C0392B').attr('stop-opacity', 0.3)
    gradient.append('stop').attr('offset', '100%').attr('stop-color', '#C0392B').attr('stop-opacity', 0)

    // Area
    const area = d3
      .area<{ time: Date; bpm: number }>()
      .x((d) => xScale(d.time))
      .y0(h)
      .y1((d) => yScale(d.bpm))
      .curve(d3.curveCatmullRom.alpha(0.5))

    g.append('path')
      .datum(parsed)
      .attr('fill', `url(#${gradientId})`)
      .attr('d', area)

    // Line
    const line = d3
      .line<{ time: Date; bpm: number }>()
      .x((d) => xScale(d.time))
      .y((d) => yScale(d.bpm))
      .curve(d3.curveCatmullRom.alpha(0.5))

    const path = g
      .append('path')
      .datum(parsed)
      .attr('fill', 'none')
      .attr('stroke', '#C0392B')
      .attr('stroke-width', 2)
      .attr('d', line)

    // Animate line drawing
    if (animated) {
      const totalLength = (path.node() as SVGPathElement)?.getTotalLength() || 0
      path
        .attr('stroke-dasharray', `${totalLength} ${totalLength}`)
        .attr('stroke-dashoffset', totalLength)
        .transition()
        .duration(2000)
        .ease(d3.easeCubicOut)
        .attr('stroke-dashoffset', 0)
    }

    // Timeline markers (vertical dashed lines)
    timeline.forEach((entry) => {
      const entryTime = new Date(entry.timestamp)
      const x = xScale(entryTime)
      if (x >= 0 && x <= w) {
        g.append('line')
          .attr('x1', x)
          .attr('x2', x)
          .attr('y1', 0)
          .attr('y2', h)
          .attr('stroke', '#6B6B80')
          .attr('stroke-width', 1)
          .attr('stroke-dasharray', '4 4')
          .attr('opacity', 0.5)

        g.append('text')
          .attr('x', x)
          .attr('y', -6)
          .attr('text-anchor', 'middle')
          .attr('fill', '#6B6B80')
          .attr('font-size', '10px')
          .text(entry.label.length > 15 ? entry.label.slice(0, 15) + '…' : entry.label)
      }
    })

    // Peak markers
    peaks.forEach((peak) => {
      const peakTime = new Date(peak.timestamp)
      const x = xScale(peakTime)
      const y = yScale(peak.bpm)

      if (x >= 0 && x <= w) {
        // Pulsing circle
        const circle = g
          .append('circle')
          .attr('cx', x)
          .attr('cy', y)
          .attr('r', 6)
          .attr('fill', '#E74C3C')
          .attr('stroke', '#fff')
          .attr('stroke-width', 2)
          .style('filter', 'drop-shadow(0 0 4px rgba(231, 76, 60, 0.6))')

        if (animated) {
          circle.attr('opacity', 0).transition().delay(2000).duration(500).attr('opacity', 1)
        }

        // BPM label
        const label = g
          .append('text')
          .attr('x', x)
          .attr('y', y - 14)
          .attr('text-anchor', 'middle')
          .attr('fill', '#F0F0F5')
          .attr('font-size', '12px')
          .attr('font-weight', 'bold')
          .text(`${peak.bpm} bpm`)

        if (animated) {
          label.attr('opacity', 0).transition().delay(2200).duration(400).attr('opacity', 1)
        }
      }
    })

    // Axes
    const xAxis = d3.axisBottom(xScale).ticks(6).tickFormat((d) => d3.timeFormat('%H:%M')(d as Date))
    g.append('g')
      .attr('transform', `translate(0,${h})`)
      .call(xAxis)
      .selectAll('text, line, path')
      .attr('color', '#6B6B80')

    const yAxis = d3.axisLeft(yScale).ticks(5).tickFormat((d) => `${d}`)
    g.append('g')
      .call(yAxis)
      .selectAll('text, line, path')
      .attr('color', '#6B6B80')

    // Y-axis label
    g.append('text')
      .attr('transform', 'rotate(-90)')
      .attr('y', -40)
      .attr('x', -h / 2)
      .attr('text-anchor', 'middle')
      .attr('fill', '#6B6B80')
      .attr('font-size', '12px')
      .text('BPM')
  }, [data, peaks, timeline, dimensions, animated])

  return (
    <div ref={containerRef} className={`w-full ${className}`}>
      <svg ref={svgRef} className="w-full" />
    </div>
  )
}
