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
import { DAYS_OF_WEEK } from '../services/storageService';
import { COLORS, FONTS, GLOBAL_STYLES } from '../theme/theme';

export default function AddRoutineModal({
  visible,
  onClose,
  exercises,
  routines = [],
  onSave,
  routineToEdit = null,
}) {
  const isEditMode = !!routineToEdit;

  const [nombre, setNombre] = useState('');
  const [selectedDays, setSelectedDays] = useState([]);
  const [selectedExercises, setSelectedExercises] = useState([]);

  // Populate form when editing
  useEffect(() => {
    if (routineToEdit) {
      setNombre(routineToEdit.nombre || '');
      setSelectedDays(routineToEdit.dias || []);
      setSelectedExercises(
        (routineToEdit.ejercicios || []).map((ex) => ({
          exerciseId: ex.exerciseId,
          series: String(ex.series || '3'),
          repeticiones: String(ex.repeticiones || '10'),
          peso: String(ex.peso || '50 kg'),
        }))
      );
    } else {
      setNombre('');
      setSelectedDays([]);
      setSelectedExercises([]);
    }
  }, [routineToEdit, visible]);

  const toggleDay = (day) => {
    if (selectedDays.includes(day)) {
      setSelectedDays(selectedDays.filter((d) => d !== day));
    } else {
      setSelectedDays([...selectedDays, day]);
    }
  };

  const toggleExerciseSelection = (exerciseId) => {
    const exists = selectedExercises.find((e) => e.exerciseId === exerciseId);
    if (exists) {
      setSelectedExercises(selectedExercises.filter((e) => e.exerciseId !== exerciseId));
    } else {
      setSelectedExercises([
        ...selectedExercises,
        { exerciseId, series: '3', repeticiones: '10', peso: '50 kg' },
      ]);
    }
  };

  const updateExerciseParam = (exerciseId, field, value) => {
    setSelectedExercises(
      selectedExercises.map((item) =>
        item.exerciseId === exerciseId ? { ...item, [field]: value } : item
      )
    );
  };

  const handleSubmit = () => {
    if (!nombre.trim()) {
      Alert.alert('CAMPO OBLIGATORIO', 'Ingresa el nombre de la rutina.');
      return;
    }
    if (selectedDays.length === 0) {
      Alert.alert('SELECCIÓN REQUERIDA', 'Selecciona al menos un día.');
      return;
    }
    if (selectedExercises.length === 0) {
      Alert.alert('SELECCIÓN REQUERIDA', 'Agrega al menos un ejercicio.');
      return;
    }

    // Check day conflicts — skip days already owned by this routine when editing
    for (const day of selectedDays) {
      const conflictingRoutine = routines.find(
        (r) => r.dias && r.dias.includes(day) && r.id !== routineToEdit?.id
      );
      if (conflictingRoutine) {
        Alert.alert(
          'DÍA ASIGNADO',
          `El día "${day.toUpperCase()}" ya pertenece a la rutina "${conflictingRoutine.nombre.toUpperCase()}".`
        );
        return;
      }
    }

    onSave({
      nombre: nombre.trim(),
      dias: selectedDays,
      ejercicios: selectedExercises,
    });

    setNombre('');
    setSelectedDays([]);
    setSelectedExercises([]);
    onClose();
  };

  return (
    <Modal visible={visible} animationType="fade" transparent={true} onRequestClose={onClose}>
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          {/* Header */}
          <View style={styles.modalHeader}>
            <View>
              <Text style={styles.tagCode}>
                {isEditMode ? 'MODIFICACIÓN DE PROGRAMA // FORMULARIO R-02' : 'DISEÑO DE PROGRAMA // FORMULARIO R-01'}
              </Text>
              <Text style={styles.modalTitle}>
                {isEditMode ? 'EDITAR RUTINA' : 'NUEVA RUTINA TÉCNICA'}
              </Text>
            </View>
            <TouchableOpacity onPress={onClose} style={styles.closeButton}>
              <Ionicons name="close-sharp" size={20} color={COLORS.softBlack} />
            </TouchableOpacity>
          </View>

          {isEditMode && (
            <View style={styles.editBadge}>
              <Ionicons name="pencil-sharp" size={11} color={COLORS.brickRed} />
              <Text style={styles.editBadgeText}>
                EDITANDO: {routineToEdit.nombre.toUpperCase()}
              </Text>
            </View>
          )}

          <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.formScroll}>
            <Text style={styles.label}>NOMBRE DE LA RUTINA *</Text>
            <TextInput
              style={styles.input}
              placeholder="EJ. EMPUJE / HIERTROFIA TORSO..."
              placeholderTextColor={COLORS.steelGray}
              value={nombre}
              onChangeText={setNombre}
            />

            <Text style={styles.label}>DÍAS ASIGNADOS (UNICIDAD DE DÍA) *</Text>
            <View style={styles.daysContainer}>
              {DAYS_OF_WEEK.map((d) => {
                const isSelected = selectedDays.includes(d);
                // Occupied = another routine owns this day (not this one being edited)
                const assignedRoutine = routines.find(
                  (r) => r.dias && r.dias.includes(d) && r.id !== routineToEdit?.id
                );
                const isOccupied = !!assignedRoutine;

                return (
                  <TouchableOpacity
                    key={d}
                    style={[
                      styles.dayChip,
                      isSelected && styles.dayChipActive,
                      isOccupied && !isSelected && styles.dayChipOccupied,
                    ]}
                    onPress={() => toggleDay(d)}
                  >
                    <Text
                      style={[
                        styles.dayChipText,
                        isSelected && styles.dayChipTextActive,
                        isOccupied && !isSelected && styles.dayChipTextOccupied,
                      ]}
                    >
                      {d.substring(0, 3).toUpperCase()}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            <Text style={styles.label}>SELECCIÓN Y CONFIGURACIÓN DE MOVIMIENTOS *</Text>
            <View style={styles.exercisesListContainer}>
              {exercises.length === 0 ? (
                <Text style={styles.emptyNotice}>NO HAY EJERCICIOS EN REGISTRO. REGISTRA AL MENOS UNO PRIMERO.</Text>
              ) : (
                exercises.map((ex) => {
                  const selectedConfig = selectedExercises.find((item) => item.exerciseId === ex.id);
                  const isSelected = !!selectedConfig;

                  return (
                    <View key={ex.id} style={[styles.exSelectionCard, isSelected && styles.exSelectionCardActive]}>
                      <TouchableOpacity
                        style={styles.exHeaderRow}
                        onPress={() => toggleExerciseSelection(ex.id)}
                      >
                        <View style={[styles.checkSquare, isSelected && styles.checkSquareActive]}>
                          <Text style={styles.checkSquareText}>{isSelected ? '✓' : ''}</Text>
                        </View>
                        <View style={styles.exTitleContainer}>
                          <Text style={styles.exTitle}>{ex.nombre.toUpperCase()}</Text>
                          <Text style={styles.exSubtitle}>
                            MÚSCULO: {ex.musculo.toUpperCase()} // TIPO: {ex.tipo.toUpperCase()}
                          </Text>
                        </View>
                      </TouchableOpacity>

                      {isSelected && (
                        <View style={styles.exConfigRow}>
                          <View style={styles.inputFieldGroup}>
                            <Text style={styles.inputFieldLabel}>SERIES:</Text>
                            <TextInput
                              style={styles.numberInput}
                              keyboardType="numeric"
                              value={String(selectedConfig.series)}
                              onChangeText={(val) => updateExerciseParam(ex.id, 'series', val)}
                            />
                          </View>

                          <View style={styles.inputFieldGroup}>
                            <Text style={styles.inputFieldLabel}>REPS:</Text>
                            <TextInput
                              style={styles.repsInput}
                              placeholder="10-12"
                              placeholderTextColor={COLORS.steelGray}
                              value={String(selectedConfig.repeticiones)}
                              onChangeText={(val) => updateExerciseParam(ex.id, 'repeticiones', val)}
                            />
                          </View>

                          <View style={styles.inputFieldGroup}>
                            <Text style={styles.inputFieldLabel}>PESO:</Text>
                            <TextInput
                              style={styles.pesoInput}
                              placeholder="50 kg"
                              placeholderTextColor={COLORS.steelGray}
                              value={String(selectedConfig.peso || '')}
                              onChangeText={(val) => updateExerciseParam(ex.id, 'peso', val)}
                            />
                          </View>
                        </View>
                      )}
                    </View>
                  );
                })
              )}
            </View>
          </ScrollView>

          <View style={styles.modalFooter}>
            <TouchableOpacity style={GLOBAL_STYLES.buttonSecondary} onPress={onClose}>
              <Text style={GLOBAL_STYLES.buttonSecondaryText}>CANCELAR</Text>
            </TouchableOpacity>

            <TouchableOpacity style={GLOBAL_STYLES.buttonPrimary} onPress={handleSubmit}>
              <Text style={GLOBAL_STYLES.buttonPrimaryText}>
                {isEditMode ? 'GUARDAR CAMBIOS' : 'GUARDAR RUTINA'}
              </Text>
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
    maxHeight: '90%',
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
  editBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.brickRed,
    paddingHorizontal: 10,
    paddingVertical: 5,
    marginBottom: 10,
    alignSelf: 'flex-start',
  },
  editBadgeText: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 10,
    fontWeight: '800',
    color: COLORS.brickRed,
    letterSpacing: 0.8,
  },
  formScroll: {
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
  input: {
    backgroundColor: COLORS.paper,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 14,
    fontWeight: '700',
  },
  daysContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
  },
  dayChip: {
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  dayChipActive: {
    backgroundColor: COLORS.brickRed,
  },
  dayChipOccupied: {
    opacity: 0.5,
  },
  dayChipText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 11,
    fontWeight: '800',
  },
  dayChipTextActive: {
    color: COLORS.paper,
  },
  dayChipTextOccupied: {
    color: COLORS.steelGray,
  },
  exercisesListContainer: {
    gap: 8,
    marginTop: 4,
  },
  emptyNotice: {
    fontFamily: FONTS.codeMono,
    color: COLORS.steelGray,
    fontSize: 10,
  },
  exSelectionCard: {
    backgroundColor: COLORS.paper,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 10,
  },
  exSelectionCardActive: {
    backgroundColor: COLORS.paperDark,
  },
  exHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  checkSquare: {
    width: 22,
    height: 22,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    backgroundColor: COLORS.paper,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkSquareActive: {
    backgroundColor: COLORS.petroleum,
  },
  checkSquareText: {
    fontFamily: FONTS.titleCondensed,
    fontWeight: '900',
    color: COLORS.paper,
    fontSize: 12,
  },
  exTitleContainer: {
    flex: 1,
  },
  exTitle: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 18,
    fontWeight: '900',
  },
  exSubtitle: {
    fontFamily: FONTS.codeMono,
    color: COLORS.steelGray,
    fontSize: 9,
  },
  exConfigRow: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 8,
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: COLORS.steelLight,
  },
  inputFieldGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  inputFieldLabel: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 10,
    fontWeight: '700',
  },
  numberInput: {
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    width: 36,
    height: 30,
    textAlign: 'center',
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 12,
    fontWeight: '800',
  },
  repsInput: {
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    width: 56,
    height: 30,
    textAlign: 'center',
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 12,
    fontWeight: '800',
  },
  pesoInput: {
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    width: 64,
    height: 30,
    textAlign: 'center',
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 12,
    fontWeight: '800',
  },
  modalFooter: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 12,
    paddingTop: 10,
    borderTopWidth: 1.5,
    borderTopColor: COLORS.softBlack,
  },
});
