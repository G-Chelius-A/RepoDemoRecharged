import React, { useState } from 'react';
import { Alert, StyleSheet, Text, TextInput, View, TouchableOpacity, ActivityIndicator } from 'react-native';
import { supabase } from '../lib/supabase';

export default function AgregaRegistros({ onRecordAdded }) {
  const [producto, setProducto] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');
  const [loading, setLoading] = useState(false);

  const manejaGuardado = async () => {
    if (!producto.trim() || !descripcion.trim() || !precio.trim()) {
      Alert.alert('Completa todos los campos antes de guardar.');
      return;
    }

    const precioNumero = Number(precio);

    if (Number.isNaN(precioNumero)) {
      Alert.alert('El precio debe ser un número válido.');
      return;
    }

    setLoading(true);

    try {
      const { data, error } = await supabase
        .from('productos')
        .insert([
          {
            producto: producto.trim(),
            descripcion: descripcion.trim(),
            precio: precioNumero,
          },
        ])
        .select();

      if (error) throw error;

      Alert.alert('Registro exitoso');
      setProducto('');
      setDescripcion('');
      setPrecio('');

      if (onRecordAdded && data && data[0]) {
        onRecordAdded(data[0]);
      }
    } catch (error) {
      Alert.alert('Error', error.message || 'Ocurrió un problema al guardar.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.card}>
      <Text style={styles.label}>Producto</Text>
      <TextInput
        style={styles.input}
        placeholder="Escribe el nombre"
        value={producto}
        onChangeText={setProducto}
        editable={!loading}
      />

      <Text style={styles.label}>Descripción</Text>
      <TextInput
        style={styles.input}
        placeholder="Escribe la descripción"
        value={descripcion}
        onChangeText={setDescripcion}
        editable={!loading}
      />

      <Text style={styles.label}>Precio</Text>
      <TextInput
        style={styles.input}
        placeholder="0.00"
        value={precio}
        keyboardType="decimal-pad"
        onChangeText={(value) => setPrecio(value.replace(/[^0-9.]/g, ''))}
        editable={!loading}
      />

      <TouchableOpacity style={styles.button} onPress={manejaGuardado} disabled={loading}>
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Guardar registro</Text>}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOpacity: 0.08,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
    marginBottom: 6,
  },
  input: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    marginBottom: 14,
    backgroundColor: '#f9fafb',
  },
  button: {
    backgroundColor: '#2563eb',
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonText: {
    color: '#ffffff',
    fontWeight: '700',
  },
});