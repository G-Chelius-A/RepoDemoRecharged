import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet } from 'react-native';
import CalculatorButton from '../components/CalculatorButton';

export default function IMCScreen() {
  const [weight, setWeight] = useState('');
  const [height, setHeight] = useState('');
  const [result, setResult] = useState(null);

  const calculate = () => {
    const kg = parseFloat(weight);
    const cm = parseFloat(height);
    if (!isNaN(kg) && !isNaN(cm) && cm > 0) {
      const meters = cm / 100;
      const bmi = kg / (meters * meters);
      let category = 'Bajo peso';
      if (bmi >= 18.5 && bmi < 25) category = 'Peso normal';
      else if (bmi >= 25 && bmi < 30) category = 'Sobrepeso';
      else if (bmi >= 30) category = 'Obesidad';
      setResult({ bmi, category });
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>⚖️ Calculadora de IMC</Text>

      <Text style={styles.label}>Peso (kg)</Text>
      <TextInput
        style={styles.input}
        keyboardType="decimal-pad"
        value={weight}
        onChangeText={setWeight}
        placeholder="Ej. 70"
      />

      <Text style={styles.label}>Estatura (cm)</Text>
      <TextInput
        style={styles.input}
        keyboardType="decimal-pad"
        value={height}
        onChangeText={setHeight}
        placeholder="Ej. 175"
      />

      <CalculatorButton title="CALCULAR IMC" onPress={calculate} />

      {result && (
        <View style={styles.resultBox}>
          <Text style={styles.result}>IMC: {result.bmi.toFixed(2)}</Text>
          <Text style={styles.category}>{result.category}</Text>
        </View>
      )}

      <Text style={styles.note}>
        El IMC es una estimación general y no sustituye una evaluación profesional.
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
  resultBox: {
    backgroundColor: '#fff', padding: 20, borderRadius: 14,
    marginTop: 25, alignItems: 'center', elevation: 2
  },
  result: { fontSize: 25, fontWeight: 'bold', color: '#182848' },
  category: { fontSize: 20, marginTop: 7, color: '#4b6cb7', fontWeight: 'bold' },
  note: { color: '#666', marginTop: 18, lineHeight: 20 }
});
