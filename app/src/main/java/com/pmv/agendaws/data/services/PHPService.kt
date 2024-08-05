package com.pmv.agendaws.data.services

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pmv.agendaws.data.models.Contacts
import com.pmv.agendaws.utils.Device
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PHPService : Response.Listener<JSONObject>, Response.ErrorListener {
    private lateinit var requestQueue: RequestQueue
    private lateinit var jsonObjectRequest: JsonObjectRequest
    private val contacts: ArrayList<Contacts> = ArrayList()
    private val serverIp: String = "http://13.56.26.211/"

    fun setContext(context: Context) {
        requestQueue = Volley.newRequestQueue(context)
    }

    fun insertarContactoWebService(contact: Contacts) {
        var url = "$serverIp/wsRegistro.php?nombre=${contact.nombre}" +
                "&telefono1=${contact.telefono1}&telefono2=${contact.telefono2}" +
                "&direccion=${contact.direccion}&notas=${contact.notas}" +
                "&favorite=${contact.favorite}&idMovil=${contact.idMovil}"
        url = url.replace(" ", "%20")
        jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, this, this)
        requestQueue.add(jsonObjectRequest)
    }

    fun actualizarContactoWebService(contact: Contacts, id: Int) {
        var url = "$serverIp/wsActualizar.php?_ID=$id" +
                "&nombre=${contact.nombre}&direccion=${contact.direccion}" +
                "&telefono1=${contact.telefono1}&telefono2=${contact.telefono2}" +
                "&notas=${contact.notas}&favorite=${contact.favorite}"
        url = url.replace(" ", "%20")
        jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, this, this)
        requestQueue.add(jsonObjectRequest)
    }

    fun borrarContactoWebService(id: Int) {
        val url = "$serverIp/wsEliminar.php?_ID=$id"
        jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, this, this)
        requestQueue.add(jsonObjectRequest)
    }

    fun consultarTodosWebService(context: Context, callback: (ArrayList<Contacts>) -> Unit) {
        val url = "$serverIp/wsConsultarTodos.php?idMovil=${Device.getSecureId(context)}"
        jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val json: JSONArray = response.optJSONArray("contactos")
                val listaContactos: ArrayList<Contacts> = ArrayList()
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
                    callback(listaContactos)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("ERROR", error.toString())
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    override fun onErrorResponse(error: VolleyError) {
        Log.i("ERROR", error.toString())
    }

    override fun onResponse(response: JSONObject) {
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
                contacts.add(contacto)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
