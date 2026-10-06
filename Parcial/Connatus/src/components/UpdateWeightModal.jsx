import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  Modal,
  TextInput,
  TouchableOpacity,
  ScrollView,
  Alert,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { updateExerciseWeightInRoutine } from '../services/storageService';
import { COLORS, FONTS, GLOBAL_STYLES } from '../theme/theme';

export default function UpdateWeightModal({ visible, onClose, routines, exercises, onSaved }) {
  const [selectedRoutineId, setSelectedRoutineId] = useState('');
  const [selectedExerciseId, setSelectedExerciseId] = useState('');
  const [nuevoPeso, setNuevoPeso] = useState('');

  useEffect(() => {
    if (routines.length > 0) {
      setSelectedRoutineId(routines[0].id);
      if (routines[0].ejercicios.length > 0) {
        setSelectedExerciseId(routines[0].ejercicios[0].exerciseId);
        setNuevoPeso(routines[0].ejercicios[0].peso || '');
      }
    }
  }, [routines, visible]);

  const handleRoutineChange = (rotId) => {
    setSelectedRoutineId(rotId);
    const rot = routines.find((r) => r.id === rotId);
    if (rot && rot.ejercicios.length > 0) {
      setSelectedExerciseId(rot.ejercicios[0].exerciseId);
      setNuevoPeso(rot.ejercicios[0].peso || '');
    } else {
      setSelectedExerciseId('');
      setNuevoPeso('');
    }
  };

  const handleExerciseChange = (exId) => {
    setSelectedExerciseId(exId);
    const rot = routines.find((r) => r.id === selectedRoutineId);
    if (rot) {
      const exRef = rot.ejercicios.find((e) => e.exerciseId === exId);
      if (exRef) {
        setNuevoPeso(exRef.peso || '');
      }
    }
  };

  const handleSave = async () => {
    if (!nuevoPeso.trim()) {
      Alert.alert('CAMPO REQUERIDO', 'Ingresa la nueva cifra de peso.');
      return;
    }

    const success = await updateExerciseWeightInRoutine(
      selectedRoutineId,
      selectedExerciseId,
      nuevoPeso.trim()
    );

    if (success) {
      Alert.alert('PESO ACTUALIZADO', 'La cifra ha sido actualizada en la ficha local.');
      onSaved();
      onClose();
    }
  };

  const currentRoutine = routines.find((r) => r.id === selectedRoutineId);

  return (
    <Modal visible={visible} animationType="fade" transparent={true} onRequestClose={onClose}>
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          {/* Header */}
          <View style={styles.modalHeader}>
            <View>
              <Text style={styles.tagCode}>MODIFICACIÓN TÉCNICA // FORMULARIO P-02</Text>
              <Text style={styles.modalTitle}>SUBIR PESO OBJETIVO</Text>
            </View>
            <TouchableOpacity onPress={onClose} style={styles.closeButton}>
              <Ionicons name="close-sharp" size={20} color={COLORS.softBlack} />
            </TouchableOpacity>
          </View>

          <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
            <Text style={styles.label}>1. SELECCIONA LA RUTINA *</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chipsScroll}>
              <View style={styles.chipsRow}>
                {routines.map((rot) => (
                  <TouchableOpacity
                    key={rot.id}
                    style={[
                      styles.chip,
                      selectedRoutineId === rot.id && styles.chipActive,
                    ]}
                    onPress={() => handleRoutineChange(rot.id)}
                  >
                    <Text
                      style={[
                        styles.chipText,
                        selectedRoutineId === rot.id && styles.chipTextActive,
                      ]}
                    >
                      {rot.nombre.toUpperCase()}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </ScrollView>

            {currentRoutine && (
              <>
                <Text style={styles.label}>2. SELECCIONA EL MOVIMIENTO *</Text>
                <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chipsScroll}>
                  <View style={styles.chipsRow}>
                    {currentRoutine.ejercicios.map((exRef) => {
                      const matchedEx = exercises.find((e) => e.id === exRef.exerciseId);
                      const isSelected = selectedExerciseId === exRef.exerciseId;
                      return (
                        <TouchableOpacity
                          key={exRef.exerciseId}
                          style={[styles.chip, isSelected && styles.chipActive]}
                          onPress={() => handleExerciseChange(exRef.exerciseId)}
                        >
                          <Text style={[styles.chipText, isSelected && styles.chipTextActive]}>
                            {matchedEx ? matchedEx.nombre.toUpperCase() : 'EJERCICIO'} ({exRef.peso || '0 KG'})
                          </Text>
                        </TouchableOpacity>
                      );
                    })}
                  </View>
                </ScrollView>
              </>
            )}

            <Text style={styles.label}>3. NUEVO PESO OBJETIVO (KG) *</Text>
            <TextInput
              style={styles.input}
              placeholder="EJ. 65 KG, 90 KG..."
              placeholderTextColor={COLORS.steelGray}
              value={nuevoPeso}
              onChangeText={setNuevoPeso}
            />
          </ScrollView>

          {/* Footer */}
          <View style={styles.modalFooter}>
            <TouchableOpacity style={GLOBAL_STYLES.buttonSecondary} onPress={onClose}>
              <Text style={GLOBAL_STYLES.buttonSecondaryText}>CANCELAR</Text>
            </TouchableOpacity>

            <TouchableOpacity style={GLOBAL_STYLES.buttonPrimary} onPress={handleSave}>
              <Text style={GLOBAL_STYLES.buttonPrimaryText}>GUARDAR NUEVO PESO</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(32, 35, 33, 0.75)',
    justifyContent: 'center',
    padding: 16,
  },
  modalContent: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 2,
    borderColor: COLORS.softBlack,
    padding: 16,
    maxHeight: '85%',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    borderBottomWidth: 1.5,
    borderBottomColor: COLORS.softBlack,
    paddingBottom: 10,
    marginBottom: 10,
  },
  tagCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
  },
  modalTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 24,
    fontWeight: '900',
    color: COLORS.softBlack,
  },
  closeButton: {
    padding: 4,
  },
  scrollContent: {
    paddingBottom: 10,
  },
  label: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    fontWeight: '800',
    color: COLORS.steelGray,
    marginBottom: 6,
    marginTop: 10,
  },
  chipsScroll: {
    marginBottom: 8,
  },
  chipsRow: {
    flexDirection: 'row',
    gap: 6,
  },
  chip: {
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  chipActive: {
    backgroundColor: COLORS.petroleum,
  },
  chipText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 11,
    fontWeight: '800',
  },
  chipTextActive: {
    color: COLORS.paper,
  },
  input: {
    backgroundColor: COLORS.paper,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 20,
    fontWeight: '900',
    marginTop: 4,
  },
  modalFooter: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 14,
    paddingTop: 10,
    borderTopWidth: 1.5,
    borderTopColor: COLORS.softBlack,
  },
});
