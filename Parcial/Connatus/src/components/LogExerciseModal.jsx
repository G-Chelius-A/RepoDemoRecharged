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
import { COLORS, FONTS, GLOBAL_STYLES } from '../theme/theme';

export default function LogExerciseModal({ visible, onClose, exerciseData, initialLog, onSave }) {
  const [pesoReal, setPesoReal] = useState('');
  const [seriesReps, setSeriesReps] = useState([]);

  useEffect(() => {
    if (exerciseData) {
      const seriesCount = parseInt(exerciseData.series, 10) || 3;

      if (initialLog && initialLog.status === 'completed' && initialLog.seriesReps) {
        setSeriesReps(initialLog.seriesReps);
        setPesoReal(initialLog.pesoReal || exerciseData.peso || '');
      } else {
        const defaultReps = exerciseData.reps ? exerciseData.reps.split('-')[0].trim() : '10';
        const initialSeriesArr = Array.from({ length: seriesCount }, (_, i) => ({
          setNum: i + 1,
          reps: defaultReps,
        }));
        setSeriesReps(initialSeriesArr);
        setPesoReal(exerciseData.peso || '');
      }
    }
  }, [exerciseData, initialLog, visible]);

  const updateSetReps = (index, value) => {
    const updated = [...seriesReps];
    updated[index].reps = value;
    setSeriesReps(updated);
  };

  const handleSave = () => {
    if (!pesoReal.trim()) {
      Alert.alert('CAMPO REQUERIDO', 'Ingresa la carga o peso utilizado.');
      return;
    }

    onSave({
      status: 'completed',
      pesoReal: pesoReal.trim(),
      seriesReps: seriesReps,
      completedAt: new Date().toISOString(),
    });
    onClose();
  };

  if (!exerciseData) return null;

  return (
    <Modal visible={visible} animationType="fade" transparent={true} onRequestClose={onClose}>
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          {/* Header */}
          <View style={styles.modalHeader}>
            <View>
              <Text style={styles.tagCode}>ANOTACIÓN DE TRABAJO // FORMULARIO S-08</Text>
              <Text style={styles.modalTitle}>REGISTRAR CARGA Y REPS</Text>
            </View>
            <TouchableOpacity onPress={onClose} style={styles.closeButton}>
              <Ionicons name="close-sharp" size={20} color={COLORS.softBlack} />
            </TouchableOpacity>
          </View>

          <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
            {/* Info Card */}
            <View style={styles.infoCard}>
              <Text style={styles.exerciseName}>{exerciseData.name}</Text>
              <Text style={styles.exerciseTarget}>
                OBJETIVO: {exerciseData.series} SERIES x {exerciseData.reps} REPS ({exerciseData.peso || 'SIN PESO'})
              </Text>
            </View>

            {/* Input Peso */}
            <Text style={styles.label}>PESO REAL UTILIZADO (KG) *</Text>
            <TextInput
              style={styles.pesoInput}
              placeholder="EJ. 60 KG, 85 KG..."
              placeholderTextColor={COLORS.steelGray}
              value={pesoReal}
              onChangeText={setPesoReal}
            />

            {/* Series */}
            <Text style={styles.label}>REPETICIONES POR SERIE *</Text>
            <View style={styles.seriesGrid}>
              {seriesReps.map((item, index) => (
                <View key={index} style={styles.serieRow}>
                  <Text style={styles.serieLabel}>SERIE {item.setNum}:</Text>
                  <View style={styles.repsInputGroup}>
                    <TextInput
                      style={styles.repsInput}
                      keyboardType="numeric"
                      value={String(item.reps)}
                      onChangeText={(val) => updateSetReps(index, val)}
                    />
                    <Text style={styles.repsSuffix}>REPS</Text>
                  </View>
                </View>
              ))}
            </View>
          </ScrollView>

          {/* Footer Buttons */}
          <View style={styles.modalFooter}>
            <TouchableOpacity style={GLOBAL_STYLES.buttonSecondary} onPress={onClose}>
              <Text style={GLOBAL_STYLES.buttonSecondaryText}>CANCELAR</Text>
            </TouchableOpacity>

            <TouchableOpacity style={GLOBAL_STYLES.buttonPrimary} onPress={handleSave}>
              <Text style={GLOBAL_STYLES.buttonPrimaryText}>ANOTAR EN FICHA</Text>
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
  infoCard: {
    backgroundColor: COLORS.paperDark,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 12,
    marginBottom: 12,
  },
  exerciseName: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 22,
    fontWeight: '900',
    color: COLORS.softBlack,
  },
  exerciseTarget: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    color: COLORS.petroleum,
    fontWeight: '800',
    marginTop: 2,
  },
  label: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    fontWeight: '800',
    color: COLORS.steelGray,
    marginBottom: 6,
    marginTop: 8,
  },
  pesoInput: {
    backgroundColor: COLORS.paper,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 20,
    fontWeight: '900',
    marginBottom: 10,
  },
  seriesGrid: {
    gap: 6,
  },
  serieRow: {
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 12,
    paddingVertical: 8,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  serieLabel: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 13,
    fontWeight: '800',
  },
  repsInputGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  repsInput: {
    backgroundColor: COLORS.paperDark,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    color: COLORS.softBlack,
    width: 48,
    height: 40,
    textAlign: 'center',
    fontFamily: FONTS.metricCondensed,
    fontSize: 16,
    fontWeight: '900',
  },
  repsSuffix: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 11,
    fontWeight: '700',
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
