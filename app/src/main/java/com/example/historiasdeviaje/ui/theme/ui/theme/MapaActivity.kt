package com.example.historiasdeviaje.ui.theme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.historiasdeviaje.databinding.ActivityMapaBinding
import com.example.historiasdeviaje.R

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtencion
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Obtén las coordenadas del Intent
        val latitud = intent.getDoubleExtra("LATITUD", 0.0)
        val longitud = intent.getDoubleExtra("LONGITUD", 0.0)

        // Agrega un marcador en la ubicación de la historia y mueve la cámara
        val ubicacionHistoria = LatLng(latitud, longitud)
        mMap.addMarker(MarkerOptions().position(ubicacionHistoria).title("Ubicación de la historia"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionHistoria, 15f))
    }
}