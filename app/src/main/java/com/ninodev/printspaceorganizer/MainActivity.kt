package com.ninodev.printspaceorganizer
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Picture
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.caverock.androidsvg.SVG
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.io.StringWriter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear un lienzo con las dimensiones deseadas
        val bitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE) // Opcional: Establecer el fondo del lienzo a blanco

        // Leer el archivo SVG desde los recursos como cadena
        val svgString = readSvgFromResource(R.raw.nube)
        Log.d("SVGDebug", "Original SVG String: $svgString")

        // Manipular el SVG
        val manipulatedSvgString = manipulateSvg(svgString)
        Log.d("SVGDebug", "Manipulated SVG String: $manipulatedSvgString")

        // Crear un nuevo SVG desde la cadena manipulada
        val newSvg = SVG.getFromString(manipulatedSvgString)
        Log.d("SVGDebug", "SVG created from manipulated string")

        // Renderizar el SVG en el lienzo
        newSvg.renderToCanvas(canvas)

        // Ahora puedes usar el bitmap donde desees, como establecerlo en una ImageView o en otro lugar
        // Por ejemplo, podrías crear una ImageView y establecer el bitmap en ella
        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)

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

    private fun manipulateSvg(svgString: String): String {
        try {
            // Parsear el SVG con DocumentBuilder
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(InputSource(StringReader(svgString)))

            // Obtener el elemento que deseas manipular
            val elements = doc.getElementsByTagName("path") // ejemplo para elementos <path>
            for (i in 0 until elements.length) {
                val element = elements.item(i) as Element

                // Modificar los atributos del elemento
                // Ejemplo: mover el elemento cambiando los atributos de transformación
                val transform = element.getAttribute("transform")
                element.setAttribute("transform", "$transform translate(0,0) scale(2) rotate(0)")
            }

            // Convertir el documento XML de nuevo a cadena
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            val result = StreamResult(StringWriter())
            val source = DOMSource(doc)
            transformer.transform(source, result)
            Log.d("SVGDebug", "SVG manipulated successfully")
            return result.writer.toString()
        } catch (e: Exception) {
            Log.e("SVGDebug", "Error manipulating SVG", e)
            return svgString
        }
    }
}
