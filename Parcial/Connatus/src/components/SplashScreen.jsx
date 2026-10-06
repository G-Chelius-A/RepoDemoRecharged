import React, { useEffect, useRef } from 'react';
import {
  StyleSheet,
  View,
  Image,
  Animated,
  TouchableOpacity,
  SafeAreaView,
  ImageBackground,
} from 'react-native';
import { COLORS } from '../theme/theme';

export default function SplashScreen({ onFinish }) {
  // Inicializamos el valor de traslación Y para que el cuadro empiece más abajo y suba
  const translateYAnim = useRef(new Animated.Value(250)).current;

  useEffect(() => {
    // Animación de subida (1.5 segundos) para el cuadro inferior
    Animated.timing(translateYAnim, {
      toValue: 0, // Llega a su posición original
      duration: 1500,
      useNativeDriver: true,
    }).start();

    // Duración total del splash screen (5 segundos)
    const timer = setTimeout(() => {
      onFinish();
    }, 5000);

    return () => clearTimeout(timer);
  }, []);

  return (
    <ImageBackground
      source={require('../../assets/background.jpeg')}
      style={styles.container}
      imageStyle={styles.bgImage}
      resizeMode="cover"
    >
      <View style={styles.overlay} />
      <SafeAreaView style={styles.safeArea}>
        <TouchableOpacity
          style={styles.touchArea}
          activeOpacity={1}
          onPress={onFinish}
        >
          <View style={styles.wrapper}>
            {/* Central Main Square Frame for logo.png (Visible y estático en todo momento) */}
            <View style={styles.mainLogoSquareFrame}>
              <Image
                source={require('../../assets/logo.png')}
                style={styles.logoImage}
                resizeMode="contain"
              />
            </View>

            {/* Bottom Smaller Square Frame for chelius.png (Sube con animación) */}
            <Animated.View
              style={[
                styles.smallCheliusSquareFrame,
                {
                  transform: [{ translateY: translateYAnim }],
                },
              ]}
            >
              <Image
                source={require('../../assets/chelius.png')}
                style={styles.cheliusImage}
                resizeMode="contain"
              />
            </Animated.View>
          </View>
        </TouchableOpacity>
      </SafeAreaView>
    </ImageBackground>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  bgImage: {
    opacity: 0.12,
  },
  overlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: COLORS.paper,
    opacity: 0.82,
  },
  safeArea: {
    flex: 1,
  },
  touchArea: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  wrapper: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  mainLogoSquareFrame: {
    width: 220,
    height: 220,
    backgroundColor: COLORS.paperCard,
    borderWidth: 2,
    borderColor: COLORS.softBlack,
    padding: 16,
    justifyContent: 'center',
    alignItems: 'center',
  },
  logoImage: {
    width: '100%',
    height: '100%',
  },
  smallCheliusSquareFrame: {
    width: 110,
    height: 110,
    backgroundColor: COLORS.paperCard,
    borderWidth: 2,
    borderColor: COLORS.softBlack,
    padding: 10,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 28,
  },
  cheliusImage: {
    width: '100%',
    height: '100%',
  },
});