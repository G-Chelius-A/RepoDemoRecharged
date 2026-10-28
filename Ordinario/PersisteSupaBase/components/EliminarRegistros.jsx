import React from 'react';
import { Alert, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { supabase } from '../lib/supabase';

export default function EliminarRegistros({ registro, onCancel, onDeleted }) {
  const eliminarRegistro = async () => {
    try {
      const { error } = await supabase.from('productos').delete().eq('id', registro.id);

      if (error) throw error;

      Alert.alert('Registro eliminado');
      if (onDeleted) onDeleted();
      if (onCancel) onCancel();
    } catch (error) {
      Alert.alert('Error al eliminar', error.message || 'No se pudo eliminar el registro.');
    }
  };

  return (
    <View style={styles.card}>
      <Text style={styles.title}>¿Deseas eliminar este registro?</Text>
      <Text style={styles.text}>{registro.producto}</Text>
      <Text style={styles.text}>{registro.descripcion}</Text>

      <View style={styles.row}>
        <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={eliminarRegistro}>
          <Text style={styles.buttonText}>Eliminar</Text>
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
    marginBottom: 8,
    color: '#111827',
  },
  text: {
    color: '#4b5563',
    marginBottom: 4,
  },
  row: {
    flexDirection: 'row',
    marginTop: 12,
  },
  button: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  dangerButton: {
    backgroundColor: '#dc2626',
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
