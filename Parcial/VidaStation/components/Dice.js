import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export default function Dice({ value }) {
  return (
    <View style={styles.dice}>
      <Text style={styles.value}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  dice: {
    width: 110, height: 110, borderRadius: 18, backgroundColor: '#fff',
    alignItems: 'center', justifyContent: 'center',
    borderWidth: 2, borderColor: '#182848', margin: 8
  },
  value: { fontSize: 58, fontWeight: 'bold', color: '#182848' }
});
