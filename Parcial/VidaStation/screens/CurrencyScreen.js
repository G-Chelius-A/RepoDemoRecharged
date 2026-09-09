import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet } from 'react-native';
import CalculatorButton from '../components/CalculatorButton';

export default function CurrencyScreen() {
  const [amount, setAmount] = useState('');
  const [rate, setRate] = useState('17.50');
  const [result, setResult] = useState(null);

  const convert = () => {
    const value = parseFloat(amount);
    const exchange = parseFloat(rate);
    if (!isNaN(value) && !isNaN(exchange)) setResult(value * exchange);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>💱 Calculadora de Divisas</Text>
      <Text style={styles.label}>Cantidad en USD</Text>
      <TextInput
        style={styles.input}
        keyboardType="decimal-pad"
        value={amount}
        onChangeText={setAmount}
        placeholder="Ej. 100"
      />
      <Text style={styles.label}>Tipo de cambio USD → MXN</Text>
      <TextInput
        style={styles.input}
        keyboardType="decimal-pad"
        value={rate}
        onChangeText={setRate}
        placeholder="Ej. 17.50"
      />
      <CalculatorButton title="CONVERTIR" onPress={convert} />
      {result !== null && (
        <Text style={styles.result}>Resultado: ${result.toFixed(2)} MXN</Text>
      )}
      <Text style={styles.note}>
        La tasa es editable para que puedas actualizarla cuando sea necesario.
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, backgroundColor: '#f5f7fb' },
  title: { fontSize: 27, fontWeight: 'bold', color: '#182848', marginBottom: 25 },
  label: { fontWeight: 'bold', marginTop: 10, color: '#444' },
  input: {
    backgroundColor: '#fff', borderWidth: 1, borderColor: '#ddd',
    borderRadius: 10, padding: 13, marginTop: 7, fontSize: 17
  },
  result: { fontSize: 23, fontWeight: 'bold', color: '#182848', marginTop: 25 },
  note: { color: '#666', marginTop: 18, lineHeight: 20 }
});
