import React, { useEffect, useRef } from 'react';
import { View, Text, Animated, StyleSheet } from 'react-native';

export default function Logo({ size = 'normal' }) {
  const big = size === 'large';
  const pulse = useRef(new Animated.Value(0.92)).current;
  const glow = useRef(new Animated.Value(0.5)).current;

  useEffect(() => {
    const animation = Animated.loop(
      Animated.parallel([
        Animated.sequence([
          Animated.timing(pulse, { toValue: 1.08, duration: 1200, useNativeDriver: true }),
          Animated.timing(pulse, { toValue: 0.96, duration: 1200, useNativeDriver: true })
        ]),
        Animated.sequence([
          Animated.timing(glow, { toValue: 1, duration: 1200, useNativeDriver: true }),
          Animated.timing(glow, { toValue: 0.5, duration: 1200, useNativeDriver: true })
        ])
      ])
    );

    animation.start();
    return () => animation.stop();
  }, [pulse, glow]);

  return (
    <View style={styles.container}>
      <Animated.View
        style={[
          styles.orbit,
          big && styles.orbitLarge,
          {
            opacity: glow,
            transform: [{ scale: pulse }]
          }
        ]}
      >
        <View style={[styles.circle, big && styles.circleLarge]}>
          <Text style={[styles.icon, big && styles.iconLarge]}>VS</Text>
        </View>
      </Animated.View>
      <Text style={[styles.title, big && styles.titleLarge]}>Vida Station</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    marginVertical: 14
  },
  orbit: {
    padding: 10,
    borderRadius: 999,
    backgroundColor: 'rgba(24, 40, 72, 0.08)',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#182848',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.2,
    shadowRadius: 12,
    elevation: 6
  },
  orbitLarge: { padding: 14 },
  circle: {
    width: 58,
    height: 58,
    borderRadius: 29,
    backgroundColor: '#182848',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: '#7dd3fc'
  },
  circleLarge: {
    width: 96,
    height: 96,
    borderRadius: 48,
    borderWidth: 3
  },
  icon: {
    color: '#fff',
    fontSize: 20,
    fontWeight: 'bold',
    letterSpacing: 1
  },
  iconLarge: { fontSize: 30 },
  title: {
    marginTop: 10,
    fontSize: 20,
    fontWeight: 'bold',
    color: '#182848',
    letterSpacing: 0.7
  },
  titleLarge: {
    fontSize: 30,
    marginTop: 12
  }
});
