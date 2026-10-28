import React, { useEffect, useState } from 'react';
import { ActivityIndicator, Alert, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { supabase } from '../lib/supabase';
import ActualizarRegistros from './ActualizarRegistros';
import EliminarRegistros from './EliminarRegistros';

export default function ConsultaRegistros({ refreshKey }) {
  const [registros, setRegistros] = useState([]);
  const [loading, setLoading] = useState(true);
  const [registroSeleccionado, setRegistroSeleccionado] = useState(null);
  const [registroEliminar, setRegistroEliminar] = useState(null);

  const obtenerRegistros = async () => {
    setLoading(true);

    try {
      const { data, error } = await supabase.from('productos').select('*').order('id', { ascending: true });

      if (error) throw error;

      setRegistros(data || []);
    } catch (error) {
      Alert.alert('Error al consultar', error.message || 'No se pudieron cargar los registros.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    obtenerRegistros();
  }, [refreshKey]);

  const manejarActualizacion = () => {
    setRegistroSeleccionado(null);
    obtenerRegistros();
  };

  const manejarEliminacion = () => {
    setRegistroEliminar(null);
    obtenerRegistros();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Consulta de registros</Text>

      {registroSeleccionado ? (
        <ActualizarRegistros
          registro={registroSeleccionado}
          onCancel={() => setRegistroSeleccionado(null)}
          onUpdated={manejarActualizacion}
        />
      ) : null}

      {registroEliminar ? (
        <EliminarRegistros
          registro={registroEliminar}
          onCancel={() => setRegistroEliminar(null)}
          onDeleted={manejarEliminacion}
        />
      ) : null}

      {loading ? (
        <ActivityIndicator size="small" color="#2563eb" style={styles.loader} />
      ) : registros.length === 0 ? (
        <Text style={styles.emptyText}>No hay registros aún.</Text>
      ) : (
        registros.map((registro) => (
          <View key={registro.id} style={styles.card}>
            <Text style={styles.itemTitle}>{registro.producto}</Text>
            <Text style={styles.itemText}>{registro.descripcion}</Text>
            <Text style={styles.itemText}>$ {Number(registro.precio || 0).toFixed(2)}</Text>

            <View style={styles.row}>
              <TouchableOpacity style={[styles.smallButton, styles.primaryButton]} onPress={() => setRegistroSeleccionado(registro)}>
                <Text style={styles.smallButtonText}>Actualizar</Text>
              </TouchableOpacity>

              <TouchableOpacity style={[styles.smallButton, styles.dangerButton]} onPress={() => setRegistroEliminar(registro)}>
                <Text style={styles.smallButtonText}>Eliminar</Text>
              </TouchableOpacity>
            </View>
          </View>
        ))
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginTop: 10,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 12,
  },
  loader: {
    marginVertical: 20,
  },
  emptyText: {
    color: '#6b7280',
    textAlign: 'center',
    marginVertical: 16,
  },
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
  itemTitle: {
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 4,
    color: '#111827',
  },
  itemText: {
    fontSize: 14,
    color: '#4b5563',
    marginBottom: 4,
  },
  row: {
    flexDirection: 'row',
    marginTop: 10,
  },
  smallButton: {
    flex: 1,
    paddingVertical: 10,
    paddingHorizontal: 14,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  primaryButton: {
    backgroundColor: '#2563eb',
  },
  dangerButton: {
    backgroundColor: '#dc2626',
    marginRight: 0,
  },
  smallButtonText: {
    color: '#fff',
    fontWeight: '600',
  },
});
