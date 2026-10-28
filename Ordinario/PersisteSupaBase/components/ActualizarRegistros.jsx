import React, { useState } from 'react';
import { Alert, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { supabase } from '../lib/supabase';

export default function ActualizarRegistros({ registro, onCancel, onUpdated }) {
  const [producto, setProducto] = useState(registro?.producto || '');
  const [descripcion, setDescripcion] = useState(registro?.descripcion || '');
  const [precio, setPrecio] = useState(String(registro?.precio ?? ''));

  const actualizarRegistro = async () => {
    if (!producto.trim() || !descripcion.trim() || !precio.trim()) {
      Alert.alert('Completa todos los campos antes de actualizar.');
      return;
    }

    const precioNumero = Number(precio);

    if (Number.isNaN(precioNumero)) {
      Alert.alert('El precio debe ser un número válido.');
      return;
    }

    try {
      const { error } = await supabase
        .from('productos')
        .update({
          producto: producto.trim(),
          descripcion: descripcion.trim(),
          precio: precioNumero,
        })
        .eq('id', registro.id);

      if (error) throw error;

      Alert.alert('Registro actualizado');
      if (onUpdated) onUpdated();
      if (onCancel) onCancel();
    } catch (error) {
      Alert.alert('Error al actualizar', error.message || 'No se pudo actualizar el registro.');
    }
  };

  return (
    <View style={styles.card}>
      <Text style={styles.title}>Actualizar registro</Text>

      <TextInput style={styles.input} value={producto} onChangeText={setProducto} placeholder="Producto" />
      <TextInput style={styles.input} value={descripcion} onChangeText={setDescripcion} placeholder="Descripción" />
      <TextInput
        style={styles.input}
        value={precio}
        onChangeText={(value) => setPrecio(value.replace(/[^0-9.]/g, ''))}
        keyboardType="decimal-pad"
        placeholder="Precio"
      />

      <View style={styles.row}>
        <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={actualizarRegistro}>
          <Text style={styles.buttonText}>Guardar</Text>
        </TouchableOpacity>

        <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={onCancel}>
          <Text style={styles.buttonText}>Cancelar</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 14,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOpacity: 0.05,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 2 },
    elevation: 1,
  },
  title: {
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 10,
    color: '#111827',
  },
  input: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 9,
    marginBottom: 8,
    backgroundColor: '#f9fafb',
  },
  row: {
    flexDirection: 'row',
    marginTop: 10,
  },
  button: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  primaryButton: {
    backgroundColor: '#2563eb',
  },
  secondaryButton: {
    backgroundColor: '#374151',
    marginRight: 0,
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600',
  },
});
