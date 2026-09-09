import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import Dice from '../components/Dice';

export default function DiceScreen() {
  const [one, setOne] = useState(1);
  const [two, setTwo] = useState(1);
  const [total, setTotal] = useState(2);

  const roll = () => {
    const a = Math.floor(Math.random() * 6) + 1;
    const b = Math.floor(Math.random() * 6) + 1;
    setOne(a); setTwo(b); setTotal(a + b);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>🎲 Dados</Text>
      <Text style={styles.subtitle}>Presiona el botón para lanzar.</Text>
      <View style={styles.row}>
        <Dice value={one} />
        <Dice value={two} />
      </View>
      <Text style={styles.total}>Total: {total}</Text>
      <TouchableOpacity style={styles.button} onPress={roll}>
        <Text style={styles.buttonText}>LANZAR DADOS</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', padding: 24, backgroundColor: '#f5f7fb' },
  title: { fontSize: 30, fontWeight: 'bold', color: '#182848' },
  subtitle: { color: '#666', margin: 8 },
  row: { flexDirection: 'row', marginTop: 25 },
  total: { fontSize: 25, fontWeight: 'bold', margin: 18, color: '#182848' },
  button: { backgroundColor: '#182848', padding: 16, borderRadius: 12 },
  buttonText: { color: '#fff', fontWeight: 'bold' }
});
