import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import AgregaRegistros from './components/AgregaRegistros';
import ConsultaRegistros from './components/ConsultaRegistros';

export default function App() {
  const [refreshKey, setRefreshKey] = useState(0);

  const handleRecordAdded = () => {
    setRefreshKey((value) => value + 1);
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <StatusBar style="auto" />
      <Text style={styles.title}>Productos</Text>
      <AgregaRegistros onRecordAdded={handleRecordAdded} />
      <ConsultaRegistros refreshKey={refreshKey} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    backgroundColor: '#f5f7fb',
    padding: 20,
    paddingBottom: 40,
  },
  title: {
    fontSize: 26,
    fontWeight: '700',
    marginBottom: 18,
    color: '#1f2937',
  },
});
