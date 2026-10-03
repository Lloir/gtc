package com.xplozder.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.xplozder.data.network.PExchangePriceHistoryModel
import com.xplozder.ui.theme.CyanElectric
import com.xplozder.ui.theme.CyanGlow
import com.xplozder.ui.theme.GoldAccent
import com.xplozder.ui.theme.ProfitGreen
import com.xplozder.ui.theme.SpaceCardBg
import com.xplozder.ui.theme.SpaceCardBorder
import com.xplozder.ui.theme.SpaceSurfaceLight
import com.xplozder.ui.theme.TextMuted
import com.xplozder.ui.theme.TextPrimary
import com.xplozder.ui.theme.TextSecondary
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3PriceTrendChart(
    history: List<PExchangePriceHistoryModel>,
    matName: String,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Surface(
            color = SpaceCardBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SpaceCardBorder),
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No historical trade ticks recorded for $matName yet.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
        return
    }

    val sortedHistory = remember(history) { history.sortedBy { it.date } }
    val latestPoint = sortedHistory.lastOrNull()
    val firstPoint = sortedHistory.firstOrNull()

    val latestPriceDollars = (latestPoint?.avgPrice ?: 0L) / 100.0
    val firstPriceDollars = (firstPoint?.avgPrice ?: 0L) / 100.0
    val deltaDollars = latestPriceDollars - firstPriceDollars
    val deltaPct = if (firstPriceDollars > 0) (deltaDollars / firstPriceDollars) * 100.0 else 0.0
    val isPositive = deltaDollars >= 0

    val isDark = com.xplozder.ui.theme.LocalIsDarkTheme.current
    val htmlData = remember(sortedHistory, matName, isDark) {
        generateD3Html(sortedHistory, matName, isDark)
    }

    Surface(
        color = SpaceCardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SpaceCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("d3_price_trend_chart_container")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = CyanElectric,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "D3 HISTORICAL PRICE FLUCTUATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanElectric,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "$%.2f", latestPriceDollars),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isPositive) Color(0x2200E676) else Color(0x22FF5252)
                ) {
                    Text(
                        text = String.format(Locale.US, "%s%.1f%% (%+.2f$)", if (isPositive) "+" else "", deltaPct, deltaDollars),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) ProfitGreen else Color(0xFFFF5252),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // D3 HTML5 / SVG Embedded WebView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(SpaceSurfaceLight, RoundedCornerShape(8.dp))
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                            }
                            webViewClient = WebViewClient()
                            loadDataWithBaseURL("https://api.g2.galactictycoons.com/", htmlData, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL("https://api.g2.galactictycoons.com/", htmlData, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("d3_webview_chart")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Touch or drag across chart to inspect daily trade ticks",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
                Text(
                    text = "D3.js v7 Engine",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanGlow
                )
            }
        }
    }
}

private fun generateD3Html(history: List<PExchangePriceHistoryModel>, matName: String, isDark: Boolean = true): String {
    val jsonArray = StringBuilder("[")
    history.forEachIndexed { i, p ->
        val priceDollars = p.avgPrice / 100.0
        val qty = p.qtySold ?: 0
        jsonArray.append("{\"date\":\"${p.date}\",\"price\":$priceDollars,\"qty\":$qty}")
        if (i < history.size - 1) jsonArray.append(",")
    }
    jsonArray.append("]")

    val bodyBg = if (isDark) "#070B14" else "#FFFFFF"
    val textColor = if (isDark) "#E0E6ED" else "#0F172A"
    val gridColor = if (isDark) "rgba(255, 255, 255, 0.08)" else "rgba(0, 0, 0, 0.06)"
    val axisTextColor = if (isDark) "#6C7D93" else "#64748B"
    val axisLineColor = if (isDark) "rgba(255, 255, 255, 0.12)" else "rgba(0, 0, 0, 0.12)"
    val lineColor = if (isDark) "#00E5FF" else "#0284C7"
    val focusCircleStroke = if (isDark) "#FFFFFF" else "#0F172A"
    val tooltipBg = if (isDark) "rgba(10, 16, 26, 0.95)" else "rgba(255, 255, 255, 0.97)"
    val tooltipBorder = if (isDark) "#00E5FF" else "#0284C7"
    val tooltipValColor = if (isDark) "#00E5FF" else "#0284C7"
    val tooltipVolColor = if (isDark) "#FFD54F" else "#B45309"
    val tooltipDateColor = if (isDark) "#8F9CAE" else "#64748B"

    return """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; user-select: none; -webkit-user-select: none; }
  body {
    background-color: $bodyBg;
    color: $textColor;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    overflow: hidden;
    width: 100vw;
    height: 100vh;
  }
  #chart-container {
    width: 100%;
    height: 100%;
    position: relative;
  }
  svg {
    width: 100%;
    height: 100%;
    overflow: visible;
  }
  .grid line {
    stroke: $gridColor;
    stroke-dasharray: 2, 2;
  }
  .axis text {
    fill: $axisTextColor;
    font-size: 10px;
    font-family: monospace;
  }
  .axis path, .axis line {
    stroke: $axisLineColor;
  }
  .line {
    fill: none;
    stroke: $lineColor;
    stroke-width: 2.5px;
    filter: drop-shadow(0 0 6px ${if (isDark) "rgba(0, 229, 255, 0.6)" else "rgba(2, 132, 199, 0.4)"});
  }
  .area {
    fill: url(#d3-gradient);
  }
  .focus-circle {
    fill: $lineColor;
    stroke: $focusCircleStroke;
    stroke-width: 2px;
    filter: drop-shadow(0 0 8px $lineColor);
  }
  .focus-line {
    stroke: ${if (isDark) "rgba(0, 229, 255, 0.4)" else "rgba(2, 132, 199, 0.4)"};
    stroke-width: 1.5px;
    stroke-dasharray: 3, 3;
  }
  .tooltip {
    position: absolute;
    top: 6px;
    right: 10px;
    background: $tooltipBg;
    border: 1px solid $tooltipBorder;
    border-radius: 6px;
    padding: 4px 8px;
    font-size: 10px;
    pointer-events: none;
    box-shadow: 0 4px 12px rgba(0,0,0,0.25);
    display: none;
    z-index: 10;
  }
  .tooltip .date { color: $tooltipDateColor; font-size: 9px; }
  .tooltip .val { color: $tooltipValColor; font-weight: bold; font-size: 12px; }
  .tooltip .vol { color: $tooltipVolColor; font-size: 9px; font-weight: 600; }
</style>
<script src="https://cdn.jsdelivr.net/npm/d3@7"></script>
</head>
<body>
<div id="chart-container">
  <div id="tooltip" class="tooltip">
    <div class="date" id="tt-date">2026-10-01</div>
    <div class="val" id="tt-price">$0.00</div>
    <div class="vol" id="tt-vol">Vol: 0</div>
  </div>
  <svg id="d3-svg"></svg>
</div>

<script>
(function() {
  const rawData = $jsonArray;
  
  function renderChart() {
    const container = document.getElementById('chart-container');
    const svg = d3.select('#d3-svg');
    svg.selectAll('*').remove();

    const width = container.clientWidth || 360;
    const height = container.clientHeight || 180;
    const margin = { top: 12, right: 16, bottom: 24, left: 40 };
    const innerWidth = width - margin.left - margin.right;
    const innerHeight = height - margin.top - margin.bottom;

    if (innerWidth <= 0 || innerHeight <= 0) return;

    const data = rawData.map(d => ({
      date: new Date(d.date),
      rawDate: d.date,
      price: +d.price,
      qty: +d.qty
    }));

    const g = svg.attr('viewBox', '0 0 ' + width + ' ' + height)
      .append('g')
      .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

    // Gradient Definition
    const defs = svg.append('defs');
    const gradient = defs.append('linearGradient')
      .attr('id', 'd3-gradient')
      .attr('x1', '0%').attr('y1', '0%')
      .attr('x2', '0%').attr('y2', '100%');
    
    gradient.append('stop')
      .attr('offset', '0%')
      .attr('stop-color', '#00E5FF')
      .attr('stop-opacity', 0.45);
    
    gradient.append('stop')
      .attr('offset', '100%')
      .attr('stop-color', '#00E5FF')
      .attr('stop-opacity', 0.0);

    // Scales
    const xExtent = d3.extent(data, d => d.date);
    const xScale = d3.scaleTime()
      .domain(xExtent[0] && xExtent[1] ? xExtent : [new Date(), new Date()])
      .range([0, innerWidth]);

    const yMin = d3.min(data, d => d.price) || 0;
    const yMax = d3.max(data, d => d.price) || 1;
    const padding = (yMax - yMin) * 0.1 || (yMin * 0.1) || 1;

    const yScale = d3.scaleLinear()
      .domain([Math.max(0, yMin - padding), yMax + padding])
      .range([innerHeight, 0]);

    // Grid lines
    g.append('g')
      .attr('class', 'grid')
      .call(d3.axisLeft(yScale)
        .ticks(3)
        .tickSize(-innerWidth)
        .tickFormat('')
      );

    // X Axis
    g.append('g')
      .attr('class', 'axis')
      .attr('transform', 'translate(0,' + innerHeight + ')')
      .call(d3.axisBottom(xScale)
        .ticks(Math.max(2, Math.floor(innerWidth / 70)))
        .tickFormat(d3.timeFormat('%m/%d'))
      );

    // Y Axis
    g.append('g')
      .attr('class', 'axis')
      .call(d3.axisLeft(yScale)
        .ticks(3)
        .tickFormat(d => '$' + d.toFixed(yMax < 10 ? 2 : 0))
      );

    // Area Generator
    const area = d3.area()
      .curve(d3.curveMonotoneX)
      .x(d => xScale(d.date))
      .y0(innerHeight)
      .y1(d => yScale(d.price));

    g.append('path')
      .datum(data)
      .attr('class', 'area')
      .attr('d', area);

    // Line Generator
    const line = d3.line()
      .curve(d3.curveMonotoneX)
      .x(d => xScale(d.date))
      .y(d => yScale(d.price));

    const path = g.append('path')
      .datum(data)
      .attr('class', 'line')
      .attr('d', line);

    // Interactive Hover / Touch Elements
    const focusLine = g.append('line')
      .attr('class', 'focus-line')
      .attr('y1', 0)
      .attr('y2', innerHeight)
      .style('display', 'none');

    const focusCircle = g.append('circle')
      .attr('class', 'focus-circle')
      .attr('r', 4.5)
      .style('display', 'none');

    const tooltip = document.getElementById('tooltip');
    const ttDate = document.getElementById('tt-date');
    const ttPrice = document.getElementById('tt-price');
    const ttVol = document.getElementById('tt-vol');

    const bisectDate = d3.bisector(d => d.date).left;

    function onPointerMove(event) {
      const coords = d3.pointer(event, g.node());
      const x0 = xScale.invert(coords[0]);
      const i = bisectDate(data, x0, 1);
      const d0 = data[i - 1];
      const d1 = data[i];
      let d = d0;
      if (d0 && d1) {
        d = x0 - d0.date > d1.date - x0 ? d1 : d0;
      } else if (!d0 && d1) {
        d = d1;
      }
      if (!d) return;

      const posX = xScale(d.date);
      const posY = yScale(d.price);

      focusLine.attr('x1', posX).attr('x2', posX).style('display', null);
      focusCircle.attr('cx', posX).attr('cy', posY).style('display', null);

      ttDate.textContent = d.rawDate;
      ttPrice.textContent = '$' + d.price.toFixed(2);
      ttVol.textContent = 'Traded: ' + d.qty.toLocaleString() + ' units';
      tooltip.style.display = 'block';
    }

    function onPointerLeave() {
      focusLine.style('display', 'none');
      focusCircle.style('display', 'none');
      tooltip.style.display = 'none';
    }

    // Touch and mouse overlay rect
    g.append('rect')
      .attr('width', innerWidth)
      .attr('height', innerHeight)
      .style('fill', 'none')
      .style('pointer-events', 'all')
      .on('mousemove touchmove', onPointerMove)
      .on('mouseleave touchend touchcancel', onPointerLeave);
  }

  // Ensure D3 is loaded or wait for it
  if (typeof d3 !== 'undefined') {
    renderChart();
  } else {
    window.addEventListener('load', renderChart);
  }

  window.addEventListener('resize', renderChart);
})();
</script>
</body>
</html>
""".trimIndent()
}
