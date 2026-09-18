import React from 'react';
import { StyleSheet, View } from 'react-native';
import { WebView } from 'react-native-webview';

export default function App() {
  const mapHtml = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
      <title>Mapa Abierto</title>
      <style>
        html, body, #map { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #f0f0f0; }
      </style>
      <link rel="stylesheet" href="https://cloudflare.com" />
      <script src="https://cloudflare.com"></script>
    </head>
    <body>
      <div id="map"></div>
      <script>
        document.addEventListener("DOMContentLoaded", function() {
          // Coordenadas iniciales [Latitud, Longitud] y nivel de zoom
          var map = L.map('map', {
            zoomControl: true,
            fadeAnimation: false
          }).setView([19.4326, -99.1332], 13);

          // Servidor de mapas CartoDB (Basado en OpenStreetMap - Compatible con túneles)
          L.tileLayer('https://{s}://{z}/{x}/{y}{r}.png', {
            maxZoom: 20,
            attribution: '© OpenStreetMap / CartoDB'
          }).addTo(map);

          // Marcador en el centro
          L.marker([19.4326, -99.1332]).addTo(map);
        });
      </script>
    </body>
    </html>
  `;

  return (
    <View style={styles.container}>
      <WebView 
        source={{ html: mapHtml }} 
        style={styles.mapa}
        originWhitelist={['*']}
        domStorageEnabled={true}
        javaScriptEnabled={true}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
  },
  mapa: {
    flex: 1,
  },
});
