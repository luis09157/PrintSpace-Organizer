package com.ninodev.printspaceorganizer
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.caverock.androidsvg.SVG
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.io.StringWriter

class MainActivity : AppCompatActivity() {

    private lateinit var canvasBitmap: Bitmap
    private lateinit var svgBitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var imageView: ImageView

    private var scaleFactor = 1.0f
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var offsetX = 0f
    private var offsetY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageView = ImageView(this)

        // Obtener dimensiones de la pantalla
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Calcular el tamaño del bitmap del SVG en función de las dimensiones físicas requeridas (90x60 cm)
        val targetWidthCm = 90f
        val targetHeightCm = 60f
        val targetDensity = displayMetrics.densityDpi.toFloat()
        val targetWidthPx = (targetWidthCm / 2.54f * targetDensity).toInt()
        val targetHeightPx = (targetHeightCm / 2.54f * targetDensity).toInt()

        // Crear un bitmap para el lienzo y su canvas
        canvasBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap)
        canvas.drawColor(Color.WHITE) // Opcional: Establecer el fondo del lienzo a blanco

        // Crear un bitmap del SVG renderizado
        val svgString = readSvgFromResource(R.raw.prueba2)
        Log.d("SVGDebug", "Original SVG String: $svgString")
        svgBitmap = renderSvgToBitmap(svgString, targetWidthPx, targetHeightPx)
        Log.d("SVGDebug", "SVG rendered to bitmap")

        // Calcular el factor de escala inicial para que el bitmap SVG se ajuste a la pantalla
        scaleFactor = screenWidth.toFloat() / targetWidthPx.toFloat()
        scaleFactor = scaleFactor.coerceAtMost(screenHeight.toFloat() / targetHeightPx.toFloat())

        // Dibujar el bitmap del SVG en el canvas con el factor de escala inicial
        canvas.save()
        canvas.scale(scaleFactor, scaleFactor)
        canvas.drawBitmap(svgBitmap, 0f, 0f, null)
        canvas.restore()

        // Configurar la vista de imagen
        imageView.setImageBitmap(canvasBitmap)
        imageView.setOnTouchListener { _, event ->
            onTouchEvent(event)
        }

        // Inicializar detector de gestos de escala
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        // Establecer la vista de imagen como la vista de contenido
        setContentView(imageView)
    }

    private fun readSvgFromResource(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val svgString = reader.use { it.readText() }
        inputStream.close()
        Log.d("SVGDebug", "SVG read from resource")
        return svgString
    }

    private fun renderSvgToBitmap(svgString: String, width: Int, height: Int): Bitmap {
        val svg = SVG.getFromString(svgString)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        svg.renderToCanvas(canvas)
        return bitmap
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector?.onTouchEvent(event)

        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY

                offsetX += dx
                offsetY += dy

                lastTouchX = event.x
                lastTouchY = event.y

                invalidateCanvas()
            }
        }
        return true
    }

    private fun invalidateCanvas() {
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scaleFactor, scaleFactor)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(svgBitmap, 0f, 0f, null)
        canvas.restore()
        imageView.setImageBitmap(canvasBitmap)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.1f, 10.0f) // Limitar el factor de escala

            invalidateCanvas()
            return true
        }
    }
}



