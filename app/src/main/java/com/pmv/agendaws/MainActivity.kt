package com.pmv.agendaws

import com.pmv.agendaws.data.models.Contacts
import com.pmv.agendaws.data.services.PHPService
import com.pmv.agendaws.utils.Device
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var btnGuardar: Button
    private lateinit var btnListar: Button
    private lateinit var btnLimpiar: Button
    private lateinit var txtNombre: EditText
    private lateinit var txtDireccion: EditText
    private lateinit var txtTelefono1: EditText
    private lateinit var txtTelefono2: EditText
    private lateinit var txtNotas: EditText
    private lateinit var cbkFavorite: CheckBox
    private var savedContacto: Contacts? = null
    private lateinit var php: PHPService
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
        setEvents()
    }

    private fun initComponents() {
        php = PHPService().apply { setContext(this@MainActivity) }
        txtNombre = findViewById(R.id.txtNombre)
        txtTelefono1 = findViewById(R.id.txtTelefono1)
        txtTelefono2 = findViewById(R.id.txtTelefono2)
        txtDireccion = findViewById(R.id.txtDireccion)
        txtNotas = findViewById(R.id.txtNotas)
        cbkFavorite = findViewById(R.id.cbxFavorito)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnListar = findViewById(R.id.btnListar)
        btnLimpiar = findViewById(R.id.btnLimpiar)
        savedContacto = null
    }

    private fun setEvents() {
        btnGuardar.setOnClickListener(this)
        btnListar.setOnClickListener(this)
        btnLimpiar.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (isNetworkAvailable()) {
            when (view.id) {
                R.id.btnGuardar -> {
                    var completo = true
                    if (txtNombre.text.toString().isEmpty()) {
                        txtNombre.error = "Introduce el Nombre"
                        completo = false
                    }

                    if (txtTelefono1.text.toString().isEmpty()) {
                        txtTelefono1.error = "Introduce el Telefono Principal"
                        completo = false
                    }

                    if (txtDireccion.text.toString().isEmpty()) {
                        txtDireccion.error = "Introduce la Direccion"
                        completo = false
                    }

                    if (completo) {
                        val nContacto = Contacts(
                            nombre = txtNombre.text.toString(),
                            telefono1 = txtTelefono1.text.toString(),
                            telefono2 = txtTelefono2.text.toString(),
                            direccion = txtDireccion.text.toString(),
                            notas = txtNotas.text.toString(),
                            favorite = if (cbkFavorite.isChecked) 1 else 0,
                            idMovil = Device.getSecureId(this)
                        )

                        if (savedContacto == null) {
                            php.insertarContactoWebService(nContacto)
                            Toast.makeText(applicationContext, R.string.mensaje, Toast.LENGTH_SHORT).show()
                            limpiar()
                        } else {
                            php.actualizarContactoWebService(nContacto, id)
                            Toast.makeText(applicationContext, R.string.mensajeedit, Toast.LENGTH_SHORT).show()
                            limpiar()
                        }
                    }
                }
                R.id.btnLimpiar -> limpiar()
                R.id.btnListar -> {
                    val i = Intent(this, ListActivity::class.java)
                    limpiar()
                    startActivityForResult(i, 0)
                }
            }
        } else {
            Toast.makeText(applicationContext, "Se necesita tener conexion a internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    private fun limpiar() {
        savedContacto = null
        txtNombre.setText("")
        txtTelefono1.setText("")
        txtTelefono2.setText("")
        txtNotas.setText("")
        txtDireccion.setText("")
        cbkFavorite.isChecked = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        intent?.extras?.let {
            if (resultCode == RESULT_OK) {
                val contacto = it.getSerializable("contacto") as Contacts
                savedContacto = contacto
                id = contacto._ID
                txtNombre.setText(contacto.nombre)
                txtTelefono1.setText(contacto.telefono1)
                txtTelefono2.setText(contacto.telefono2)
                txtDireccion.setText(contacto.direccion)
                txtNotas.setText(contacto.notas)
                cbkFavorite.isChecked = contacto.favorite > 0
            } else {
                limpiar()
            }
        }
    }
}
