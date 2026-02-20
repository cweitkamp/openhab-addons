# GrünstromIndex Binding

https://gruenstromindex.de/

The GrünstromIndex (Green Power Index) is a measure used to indicate the availability of renewable energy in the electricity grid at any given time.
It is calculated based on real-time data about the share of renewable energy sources, such as wind and solar power, in the overall energy mix.
The GrünstromIndex provides forecasts and current status updates to help consumers, businesses, and energy managers optimize their electricity usage based on when the grid is supplied with the most green energy.
By integrating the GrünstromIndex into energy management systems, users can automate the scheduling of energy-intensive tasks—like charging electric vehicles or heating water—during periods when renewable energy is most abundant, reducing carbon footprints and enhancing grid stability.
An outline with background methodology is available at: https://corrently.io/books/grunstromindex

## Supported Things

- `account`: Provides access to the GrünstromIndex API.
- `green-energy-forecast`: Provides detailed information about the forecasted CO2 emissions for a specific time period and a specific area in Germany.

## Thing Configuration

An optional Access token can be retrieved from https://console.corrently.io/ .

### `account` Thing Configuration

| Name            | Type    | Description                                                   | Default | Required | Advanced |
|-----------------|---------|---------------------------------------------------------------|---------|----------|----------|
| token           | text    | Access token to access the GrünstromIndex API (if available). | N/A     | no       | no       |
| refreshInterval | integer | Specifies the refresh interval (in minutes).                  |      30 | no       | no       |

### `green-energy-forecast` Thing Configuration

| Name    | Type | Description                             | Default | Required | Advanced |
|---------|------|-----------------------------------------|---------|----------|----------|
| zipcode | text | Zipcode of a city / village in Germany. | N/A     | yes      | no       |

## Channels

The `account` Thing has no Channels.

The `green-energy-forecast` Thing has the following Channels:

| Channel                 | Type                     | Read/Write | Description                                                          |
|-------------------------|--------------------------|------------|----------------------------------------------------------------------|
| gruenstromindex         | Number:EmissionIntensity | R          | Forecasted Green Energy Index (GrünstromIndex). Supports TimeSeries. |
| carbondioxide-emissions | Number                   | R          | Forecasted CO2 emissions. Supports TimeSeries.                       |

## Example

### Things



```
Thing gruenstromindex:account:account "GrünstromIndex Konto" @ "WebService" [refreshInterval=60]

Thing gruenstromindex:green-energy-forecast:account:local "GrünstromIndex Vorhersage" (gruenstromindex:account:account) @ "WebService" [zipcode="#####"]
```

### Items

```
Number electricityForecastGruenstromIndex "Vorhergesagter GrünstromIndex" <carbondioxide> (gFORECAST) [Forecast] { channel="gruenstromindex:green-energy-forecast:account:local:energy-forecast#gruenstromindex" }

String electricityForecastGruenstromIndexTransformed "Vorhergesagter GrünstromIndex [%.0f]" <carbondioxide> [Forecast] { channel="gruenstromindex:green-energy-forecast:account:local:energy-forecast#gruenstromindex" [profile="transform:SCALE", function="gruenstromindex.scale", sourceFormat="%s"] }

Number:EmissionIntensity electricityForecastCO2EmissionsPerUnit "Vorhergesagte CO₂-Emissionen pro kWh [%.1f %unit%]" <carbondioxide> (gFORECAST) [CO2, Forecast] { channel="gruenstromindex:green-energy-forecast:account:local:energy-forecast#carbondioxide-emissions" }
```


### Scale transformation

`gruenstromindex.scale`

```
[..40[=Hoch
[40..60]=Mittel
]60..]=Niegrig
NaN=Unbekannt
```

### Chart

```yaml
config:
  chartType: ""
  future: 0.75
  label: CO₂-Emissionen - Vorhersage
  period: 12h
  sidebar: false
slots:
  dataZoom:
    - component: oh-chart-datazoom
      config:
        bottom: "3"
        orient: horizontal
        show: true
        type: slider
  grid:
    - component: oh-chart-grid
      config:
        includeLabels: true
  legend:
    - component: oh-chart-legend
      config:
        bottom: 3
        type: scroll
  series:
    - component: oh-time-series
      config:
        color: darkgreen
        gridIndex: 0
        item: electricityForecastGruenstromIndex
        markPoint:
          data:
            - name: Minimum
              type: min
              itemStyle:
                color: red
            - name: Maximum
              type: max
              itemStyle:
                color: green
        name: GrünstromIndex
        type: bar
        xAxisIndex: 0
        yAxisIndex: 1
    - component: oh-time-series
      config:
        areaStyle:
          opacity: 0.33
        color: lightgreen
        gridIndex: 0
        item: electricityForecastCO2EmissionsPerUnit
        markLine:
          data:
            - name: Durchschnitt
              type: average
        markPoint:
          data:
            - name: Minimum
              type: min
            - name: Maximum
              type: max
        markers:
          - time
        name: CO₂-Emissionen (in g/kWh)
        type: line
        xAxisIndex: 0
        yAxisIndex: 0
  tooltip:
    - component: oh-chart-tooltip
      config:
        show: true
  visualMap:
    - component: oh-chart-visualmap
      config:
        pieces:
          - color: red
            label: Hoch
            lt: 40
          - color: yellow
            label: Mittel
            gte: 40
            lte: 60
          - color: green
            label: Niedrig
            gt: 60
        seriesIndex: 0
        top: middle
        left: right
  xAxis:
    - component: oh-time-axis
      config:
        gridIndex: 0
  yAxis:
    - component: oh-value-axis
      config:
        gridIndex: 0
        max: "500"
        min: "0"
        minorSplitLine:
          show: true
        minorTick:
          show: true
        name: g/kWh
        scale: false
        splitArea:
          show: true
    - component: oh-value-axis
      config:
        gridIndex: 0
        interval: 10
        max: "100"
        min: 0
        minorTick:
          show: true
        name: GrünstromIndex
        splitLine:
          show: false
```

## Any custom content here!

Color code definition of provider: https://corrently.io/books/grunstromindex/page/eaf-10-dynamische-tarife-fur-elektrizitat

| Level |  GSI  | Color  | OBIS Code |
|-------|-------|--------|-----------|
|     0 |       |        |     1.8.0 |
|     1 |  <40  | Red    |     1.8.1 |
|     2 | 40-60 | Yellow |     1.8.2 |
|     3 |  >60  | Green  |     1.8.3 |
