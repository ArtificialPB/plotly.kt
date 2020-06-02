package histogram

import scientifik.plotly.Plotly
import scientifik.plotly.makeFile
import scientifik.plotly.models.AxisType
import scientifik.plotly.trace
import java.util.*


fun main() {
    val rnd = Random()
    val values = List(500){rnd.nextDouble()}

    val plot = Plotly.plot2D{
        trace(values){
            name = "Random data"
            type = AxisType.histogram

            cumulative {
                enabled = true
            }
        }
        layout {
            title = "Cumulative Histogram"
            xaxis {
                title = "Bins"
            }
            yaxis {
                title = "Sum of height"
            }
        }
    }

    plot.makeFile()
}