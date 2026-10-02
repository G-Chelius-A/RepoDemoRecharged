import React, { useState, useRef } from 'react';
import { StyleSheet, Text, View, TouchableOpacity, Image, Alert } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import * as MediaLibrary from 'expo-media-library/legacy'; // <-- Solución al error nativo
import { detectFaceFromBase64 } from './faceDetector';

export default function App() {
  const [cameraPermission, requestCameraPermission] = useCameraPermissions();
  const [libraryPermission, requestLibraryPermission] = MediaLibrary.usePermissions();

  const [photoUri, setPhotoUri] = useState(null);
  const [detectionMessage, setDetectionMessage] = useState('');
  const [detectionError, setDetectionError] = useState(false);
  const cameraRef = useRef(null);

  if (!cameraPermission || !libraryPermission) {
    return (
      <View style={styles.container}>
        <Text style={styles.text}>Cargando permisos del sistema...</Text>
      </View>
    );
  }

  if (!cameraPermission.granted || !libraryPermission.granted) {
    return (
      <View style={styles.container}>
        <Text style={styles.text}>
          Necesitamos accesos para poder escanear tu rostro y guardar la imagen.
        </Text>

        {!cameraPermission.granted && (
          <TouchableOpacity style={[styles.button, { marginBottom: 10 }]} onPress={requestCameraPermission}>
            <Text style={styles.buttonText}>Permitir uso de Cámara</Text>
          </TouchableOpacity>
        )}

        {!libraryPermission.granted && (
          <TouchableOpacity style={styles.button} onPress={requestLibraryPermission}>
            <Text style={styles.buttonText}>Permitir acceso a Galería</Text>
          </TouchableOpacity>
        )}
      </View>
    );
  }

  const takePicture = async () => {
    if (cameraRef.current) {
      try {
        const options = { quality: 0.8, base64: true };
        const photo = await cameraRef.current.takePictureAsync(options);
        setPhotoUri(photo.uri);
        setDetectionMessage('Analizando rostro...');
        setDetectionError(false);

        await new Promise((resolve) => setTimeout(resolve, 50));

        try {
          const faceDetected = detectFaceFromBase64(photo.base64);
          setDetectionMessage(faceDetected ? 'Rostro Detectado' : 'No se detectó ningún rostro');
          setDetectionError(!faceDetected);
        } catch (error) {
          console.error('Error en la detección local: ', error);
          setDetectionMessage('No se pudo analizar la foto');
          setDetectionError(true);
        }

        await MediaLibrary.saveToLibraryAsync(photo.uri);

        Alert.alert('¡Éxito!', 'La selfie ha sido capturada y guardada en tu galería.');
      } catch (error) {
        console.error('Error en el proceso de guardado: ', error);
        Alert.alert('Error', 'No se pudo guardar o analizar la imagen en el dispositivo.');
      }
    }
  };

  return (
    <View style={styles.container}>
      {photoUri ? (
        <View style={styles.previewContainer}>
          <Image source={{ uri: photoUri }} style={styles.previewImage} />
          {!!detectionMessage && (
            <Text style={[styles.detectionMessage, detectionError && styles.detectionError]}>
              {detectionMessage}
            </Text>
          )}
          <TouchableOpacity
            style={styles.button}
            onPress={() => {
              setPhotoUri(null);
              setDetectionMessage('');
              setDetectionError(false);
            }}
          >
            <Text style={styles.buttonText}>Tomar otra foto</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <CameraView style={StyleSheet.absoluteFillObject} facing="front" ref={cameraRef}>
          <View style={styles.overlayContainer}>
            <View style={styles.faceOvalGuide} />
            <TouchableOpacity style={styles.captureButton} onPress={takePicture}>
              <View style={styles.innerCaptureButton} />
            </TouchableOpacity>
          </View>
        </CameraView>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#000', justifyContent: 'center', alignItems: 'center' },
  text: { color: '#fff', textAlign: 'center', marginBottom: 20, paddingHorizontal: 20, fontSize: 16 },
  button: { backgroundColor: '#1E90FF', paddingVertical: 12, paddingHorizontal: 24, borderRadius: 8, minWidth: 220, alignItems: 'center' },
  buttonText: { color: '#fff', fontWeight: 'bold' },
  overlayContainer: { flex: 1, backgroundColor: 'transparent', justifyContent: 'space-between', alignItems: 'center', paddingBottom: 40, paddingTop: 100 },
  faceOvalGuide: { width: 250, height: 320, borderWidth: 2, borderColor: 'rgba(255, 255, 255, 0.6)', borderRadius: 125, borderStyle: 'dashed' },
  captureButton: { width: 74, height: 74, borderRadius: 37, borderWidth: 4, borderColor: '#fff', justifyContent: 'center', alignItems: 'center' },
  innerCaptureButton: { width: 56, height: 56, borderRadius: 28, backgroundColor: '#fff' },
  previewContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', width: '100%' },
  previewImage: { width: '90%', height: '70%', borderRadius: 16, marginBottom: 20, resizeMode: 'contain' },
  detectionMessage: { color: '#34d399', fontSize: 20, fontWeight: 'bold', marginBottom: 16 },
  detectionError: { color: '#fca5a5' },
});
