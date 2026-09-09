import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';

export default function CalculatorButton({ title, onPress }) {
  return (
    <TouchableOpacity style={styles.button} onPress={onPress}>
      <Text style={styles.text}>{title}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  button: {
    backgroundColor: '#182848',
    padding: 14,
    borderRadius: 12,
    marginTop: 10,
    alignItems: 'center'
  },
  text: { color: '#fff', fontSize: 16, fontWeight: 'bold' }
});
