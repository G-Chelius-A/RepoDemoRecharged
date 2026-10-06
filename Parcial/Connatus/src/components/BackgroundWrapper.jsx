import React from 'react';
import { StyleSheet, View, ImageBackground } from 'react-native';
import { COLORS } from '../theme/theme';

/**
 * Wraps any screen with background.jpeg at reduced opacity.
 * Usage: replace <SafeAreaView> with <BackgroundWrapper> and pass style if needed.
 */
export default function BackgroundWrapper({ children, style }) {
  return (
    <ImageBackground
      source={require('../../assets/background.jpeg')}
      style={[styles.root, style]}
      imageStyle={styles.bgImage}
      resizeMode="cover"
    >
      {/* Semi-transparent paper overlay to maintain editorial feel */}
      <View style={styles.overlay} />
      {children}
    </ImageBackground>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  bgImage: {
    opacity: 0.25,
  },
  overlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: COLORS.paper,
    opacity: 0.82,
  },
});
