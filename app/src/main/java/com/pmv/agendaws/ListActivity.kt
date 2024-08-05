package com.pmv.agendaws

import android.app.Activity
import android.app.AlertDialog
import android.app.ListActivity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pmv.agendaws.data.models.Contacts
import com.pmv.agendaws.data.services.PHPService
import com.pmv.agendaws.utils.Device
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ListActivity : ListActivity(), Response.Listener<JSONObject>, Response.ErrorListener {
    private lateinit var btnNuevo: Button
    private val context: Context = this
    private val php: PHPService = PHPService()
    private lateinit var request: RequestQueue
    private lateinit var jsonObjectRequest: JsonObjectRequest
    private val listaContactos: ArrayList<Contacts> = ArrayList()
    private val serverip: String = "http://13.56.26.211/"
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        request = Volley.newRequestQueue(context)
        progressDialog = ProgressDialog(this).apply {
            setMessage("Cargando...")
            setCancelable(false)
        }
        consultarTodosWebService()
        btnNuevo = findViewById(R.id.btnNuevo)
        btnNuevo.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun consultarTodosWebService() {
        progressDialog.show()
        val url = "$serverip/wsConsultarTodos.php?idMovil=${Device.getSecureId(this)}"
        jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, this, this)
        request.add(jsonObjectRequest)
    }

    override fun onErrorResponse(error: VolleyError) {
        progressDialog.dismiss()
        Log.e("ERROR", error.toString())
    }

    override fun onResponse(response: JSONObject) {
        progressDialog.dismiss()
        val json: JSONArray = response.optJSONArray("contactos")
        try {
            for (i in 0 until json.length()) {
                val contacto = Contacts()
                val jsonObject: JSONObject = json.getJSONObject(i)
                contacto._ID = jsonObject.optInt("_ID")
                contacto.nombre = jsonObject.optString("nombre")
                contacto.telefono1 = jsonObject.optString("telefono1")
                contacto.telefono2 = jsonObject.optString("telefono2")
                contacto.direccion = jsonObject.optString("direccion")
                contacto.notas = jsonObject.optString("notas")
                contacto.favorite = jsonObject.optInt("favorite")
                contacto.idMovil = jsonObject.optString("idMovil")
                listaContactos.add(contacto)
            }
            val adapter = MyArrayAdapter(context, R.layout.layout_contact, listaContactos)
            listAdapter = adapter
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    inner class MyArrayAdapter(
        context: Context,
        private val textViewResourceId: Int,
        private val objects: ArrayList<Contacts>
    ) : ArrayAdapter<Contacts>(context, textViewResourceId, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(this.textViewResourceId, null)

            val lblNombre = view.findViewById<TextView>(R.id.lblNombreContacto)
            val lblTelefono = view.findViewById<TextView>(R.id.lblTelefonoContacto)
            val btnModificar = view.findViewById<Button>(R.id.btnModificar)
            val btnBorrar = view.findViewById<Button>(R.id.btnBorrar)

            if (objects[position].favorite > 0) {
                lblNombre.setTextColor(Color.BLUE)
                lblTelefono.setTextColor(Color.BLUE)
            } else {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            }

            lblNombre.text = objects[position].nombre
            lblTelefono.text = objects[position].telefono1

            btnBorrar.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Confirmar eliminación")
                builder.setMessage("¿Está seguro de que desea eliminar a ${objects[position].nombre}?")

                builder.setPositiveButton("Sí") { dialog, which ->
                    php.setContext(context)
                    Log.i("id", objects[position]._ID.toString())
                    php.borrarContactoWebService(objects[position]._ID)
                    objects.removeAt(position)
                    notifyDataSetChanged()
                    Toast.makeText(getApplicationContext(), "Contacto eliminado con exito", Toast.LENGTH_SHORT).show()
                }

                builder.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }

                val alert = builder.create()
                alert.show()
            }

            btnModificar.setOnClickListener {
                val oBundle = Bundle()
                oBundle.putSerializable("contacto", objects[position])
                val i = Intent()
                i.putExtras(oBundle)
                setResult(Activity.RESULT_OK, i)
                finish()
            }

            return view
        }
    }
}
