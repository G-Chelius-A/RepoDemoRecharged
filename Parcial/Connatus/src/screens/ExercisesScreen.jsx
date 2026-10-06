import React, { useState, useEffect, useCallback } from 'react';
import {
  StyleSheet,
  View,
  Text,
  ScrollView,
  TextInput,
  TouchableOpacity,
  Image,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import {
  getExercises,
  addExercise,
  updateExercise,
  deleteExercise,
  getRoutines,
  addRoutine,
  updateRoutine,
  deleteRoutine,
  MUSCLES,
  TYPES,
  DAYS_OF_WEEK,
} from '../services/storageService';
import AddExerciseModal from '../components/AddExerciseModal';
import AddRoutineModal from '../components/AddRoutineModal';
import { COLORS, FONTS, GLOBAL_STYLES } from '../theme/theme';
import BackgroundWrapper from '../components/BackgroundWrapper';

export default function ExercisesScreen() {
  const [activeTab, setActiveTab] = useState('ejercicios');
  const [loading, setLoading] = useState(true);

  // Exercises State
  const [exercises, setExercises] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedMuscleFilter, setSelectedMuscleFilter] = useState('Todos');
  const [selectedTipoFilter, setSelectedTipoFilter] = useState('Todos');
  const [isAddExerciseVisible, setIsAddExerciseVisible] = useState(false);
  const [exerciseToEdit, setExerciseToEdit] = useState(null);

  // Routines State
  const [routines, setRoutines] = useState([]);
  const [selectedDayFilter, setSelectedDayFilter] = useState('Todos');
  const [isAddRoutineVisible, setIsAddRoutineVisible] = useState(false);
  const [routineToEdit, setRoutineToEdit] = useState(null);

  const loadData = async () => {
    setLoading(true);
    const exData = await getExercises();
    const rotData = await getRoutines();
    setExercises(exData);
    setRoutines(rotData);
    setLoading(false);
  };

  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [])
  );

  // Handle Add or Edit Exercise
  const handleSaveExercise = async (exData, isEditing) => {
    if (isEditing) {
      const updated = await updateExercise(exData.id, exData);
      if (updated) {
        loadData();
        Alert.alert('FICHA ACTUALIZADA', `Ejercicio "${updated.nombre.toUpperCase()}" modificado.`);
      }
    } else {
      const created = await addExercise(exData);
      if (created) {
        loadData();
        Alert.alert('FICHA GUARDADA', `Ejercicio "${created.nombre.toUpperCase()}" registrado.`);
      }
    }
  };

  const handleOpenEdit = (ex) => {
    setExerciseToEdit(ex);
    setIsAddExerciseVisible(true);
  };

  const handleOpenCreate = () => {
    setExerciseToEdit(null);
    setIsAddExerciseVisible(true);
  };

  const handleDeleteExercise = (id, nombre) => {
    Alert.alert(
      'ELIMINAR MOVIMIENTO',
      `¿Deseas dar de baja el ejercicio "${nombre.toUpperCase()}" de las fichas locales?`,
      [
        { text: 'CANCELAR', style: 'cancel' },
        {
          text: 'ELIMINAR',
          style: 'destructive',
          onPress: async () => {
            await deleteExercise(id);
            loadData();
          },
        },
      ]
    );
  };

  const handleSaveRoutine = async (rotData) => {
    if (routineToEdit) {
      const updated = await updateRoutine(routineToEdit.id, rotData);
      if (updated) {
        loadData();
        Alert.alert('RUTINA ACTUALIZADA', `Ficha "${updated.nombre.toUpperCase()}" modificada.`);
      }
    } else {
      const created = await addRoutine(rotData);
      if (created) {
        loadData();
        Alert.alert('RUTINA GUARDADA', `Ficha de rutina "${created.nombre.toUpperCase()}" creada.`);
      }
    }
    setRoutineToEdit(null);
  };

  const handleOpenEditRoutine = (rot) => {
    setRoutineToEdit(rot);
    setIsAddRoutineVisible(true);
  };

  const handleOpenCreateRoutine = () => {
    setRoutineToEdit(null);
    setIsAddRoutineVisible(true);
  };

  const handleDeleteRoutine = (id, nombre) => {
    Alert.alert(
      'ELIMINAR RUTINA',
      `¿Deseas dar de baja la rutina "${nombre.toUpperCase()}"?`,
      [
        { text: 'CANCELAR', style: 'cancel' },
        {
          text: 'ELIMINAR',
          style: 'destructive',
          onPress: async () => {
            await deleteRoutine(id);
            loadData();
          },
        },
      ]
    );
  };

  const filteredExercises = exercises.filter((ex) => {
    const matchesSearch = ex.nombre.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesMuscle = selectedMuscleFilter === 'Todos' || ex.musculo === selectedMuscleFilter;
    const matchesTipo = selectedTipoFilter === 'Todos' || ex.tipo === selectedTipoFilter;
    return matchesSearch && matchesMuscle && matchesTipo;
  });

  const filteredRoutines = routines.filter((rot) => {
    if (selectedDayFilter === 'Todos') return true;
    return rot.dias.includes(selectedDayFilter);
  });

  return (
    <BackgroundWrapper>
      <SafeAreaView style={styles.container}>
        <View style={styles.content}>
          {/* Editorial Top Header */}
          <View style={styles.header}>
            <Text style={styles.tagCode}>ÍNDICE GENERAL // BIBLIOTECA FÍSICA</Text>
            <Text style={styles.headerTitle}>FICHAS Y RUTINAS</Text>
            <Text style={styles.headerSubtitle}>CATÁLOGO UTILITARIO DE MOVIMIENTOS Y SESIONES</Text>
          </View>

          {/* Tab Selector */}
          <View style={styles.segmentContainer}>
            <TouchableOpacity
              style={[styles.segmentButton, activeTab === 'ejercicios' && styles.segmentButtonActive]}
              onPress={() => setActiveTab('ejercicios')}
            >
              <Text style={[styles.segmentText, activeTab === 'ejercicios' && styles.segmentTextActive]}>
                01 // MOVIMIENTOS ({exercises.length})
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.segmentButton, activeTab === 'rutinas' && styles.segmentButtonActive]}
              onPress={() => setActiveTab('rutinas')}
            >
              <Text style={[styles.segmentText, activeTab === 'rutinas' && styles.segmentTextActive]}>
                02 // RUTINAS ({routines.length})
              </Text>
            </TouchableOpacity>
          </View>

          {loading ? (
            <View style={styles.loadingContainer}>
              <ActivityIndicator size="large" color={COLORS.brickRed} />
              <Text style={styles.loadingText}>CARGANDO FICHAS DE TRABAJO...</Text>
            </View>
          ) : activeTab === 'ejercicios' ? (
            /* ================= MIS EJERCICIOS TAB ================= */
            <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
              {/* Create Button */}
              <View style={styles.createBar}>
                <TouchableOpacity
                  style={GLOBAL_STYLES.buttonPrimary}
                  onPress={handleOpenCreate}
                >
                  <Text style={GLOBAL_STYLES.buttonPrimaryText}>+ ALTA DE NUEVO MOVIMIENTO</Text>
                </TouchableOpacity>
              </View>

              {/* Search Box */}
              <View style={styles.searchContainer}>
                <Ionicons name="search-sharp" size={16} color={COLORS.softBlack} />
                <TextInput
                  style={styles.searchInput}
                  placeholder="BUSCAR FICHA POR NOMBRE..."
                  placeholderTextColor={COLORS.steelGray}
                  value={searchQuery}
                  onChangeText={setSearchQuery}
                />
                {searchQuery.length > 0 && (
                  <TouchableOpacity onPress={() => setSearchQuery('')}>
                    <Ionicons name="close-sharp" size={16} color={COLORS.softBlack} />
                  </TouchableOpacity>
                )}
              </View>

              {/* Filter by Category */}
              <Text style={styles.filterSectionTitle}>CATEGORÍA DE MOVIMIENTO:</Text>
              <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.filterScroll}>
                <View style={styles.filterRow}>
                  {['Todos', ...TYPES].map((tipo) => (
                    <TouchableOpacity
                      key={tipo}
                      style={[
                        styles.filterChip,
                        selectedTipoFilter === tipo && styles.filterChipActive,
                      ]}
                      onPress={() => setSelectedTipoFilter(tipo)}
                    >
                      <Text
                        style={[
                          styles.filterChipText,
                          selectedTipoFilter === tipo && styles.filterChipTextActive,
                        ]}
                      >
                        {tipo.toUpperCase()}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </View>
              </ScrollView>

              {/* Filter by Muscle */}
              <Text style={styles.filterSectionTitle}>MÚSCULO OBJETIVO:</Text>
              <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.filterScroll}>
                <View style={styles.filterRow}>
                  {['Todos', ...MUSCLES].map((musculo) => (
                    <TouchableOpacity
                      key={musculo}
                      style={[
                        styles.filterChip,
                        selectedMuscleFilter === musculo && styles.filterChipActive,
                      ]}
                      onPress={() => setSelectedMuscleFilter(musculo)}
                    >
                      <Text
                        style={[
                          styles.filterChipText,
                          selectedMuscleFilter === musculo && styles.filterChipTextActive,
                        ]}
                      >
                        {musculo.toUpperCase()}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </View>
              </ScrollView>

              <Text style={GLOBAL_STYLES.sectionTitle}>
                REGISTROS ENCONTRADOS ({filteredExercises.length})
              </Text>

              {filteredExercises.length > 0 ? (
                filteredExercises.map((ex, index) => (
                  <View key={ex.id} style={styles.exerciseCard}>
                    {/* CONATUS FRAMED EXERCISE IMAGE */}
                    <View style={styles.framedImageContainer}>
                      <Image source={{ uri: ex.imagen }} style={styles.framedImage} resizeMode="cover" />
                      <View style={styles.imageCornerTag}>
                        <Text style={styles.imageCornerTagText}>FIG. 0{index + 1}</Text>
                      </View>
                    </View>

                    <View style={styles.cardInfo}>
                      <Text style={styles.cardCode}>MOV-{index + 1 < 10 ? `0${index + 1}` : index + 1} // {ex.musculo.toUpperCase()}</Text>
                      <Text style={styles.cardTitle}>{ex.nombre.toUpperCase()}</Text>

                      <View style={styles.badgesRow}>
                        <View style={GLOBAL_STYLES.petroleumBadge}>
                          <Text style={GLOBAL_STYLES.petroleumBadgeText}>{ex.tipo.toUpperCase()}</Text>
                        </View>
                      </View>
                    </View>

                    <View style={styles.cardActions}>
                      <TouchableOpacity
                        style={styles.actionButton}
                        onPress={() => handleOpenEdit(ex)}
                      >
                        <Ionicons name="pencil-sharp" size={16} color={COLORS.petroleum} />
                      </TouchableOpacity>

                      <TouchableOpacity
                        style={styles.actionButton}
                        onPress={() => handleDeleteExercise(ex.id, ex.nombre)}
                      >
                        <Ionicons name="trash-outline" size={16} color={COLORS.brickRed} />
                      </TouchableOpacity>
                    </View>
                  </View>
                ))
              ) : (
                <View style={styles.emptyContainer}>
                  <Ionicons name="document-text-outline" size={36} color={COLORS.steelGray} />
                  <Text style={styles.emptyText}>NO SE ENCONTRARON FICHAS CON LOS FILTROS SELECCIONADOS.</Text>
                </View>
              )}
            </ScrollView>
          ) : (
            /* ================= MIS RUTINAS TAB ================= */
            <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
              <View style={styles.createBar}>
                <TouchableOpacity
                  style={GLOBAL_STYLES.buttonPrimary}
                  onPress={() => setIsAddRoutineVisible(true)}
                >
                  <Text style={GLOBAL_STYLES.buttonPrimaryText}>+ CREAR NUEVA RUTINA </Text>
                </TouchableOpacity>
              </View>

              <Text style={styles.filterSectionTitle}>DÍA DE LA SEMANA:</Text>
              <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.filterScroll}>
                <View style={styles.filterRow}>
                  {['Todos', ...DAYS_OF_WEEK].map((day) => (
                    <TouchableOpacity
                      key={day}
                      style={[
                        styles.filterChip,
                        selectedDayFilter === day && styles.filterChipActive,
                      ]}
                      onPress={() => setSelectedDayFilter(day)}
                    >
                      <Text
                        style={[
                          styles.filterChipText,
                          selectedDayFilter === day && styles.filterChipTextActive,
                        ]}
                      >
                        {day.toUpperCase()}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </View>
              </ScrollView>

              <Text style={GLOBAL_STYLES.sectionTitle}>
                RUTINAS REGISTRADAS ({filteredRoutines.length})
              </Text>

              {filteredRoutines.length > 0 ? (
                filteredRoutines.map((rot, idx) => (
                  <View key={rot.id} style={styles.routineCard}>
                    <View style={styles.routineHeaderRow}>
                      <View style={{ flex: 1 }}>
                        <Text style={styles.routineCode}>FICHA DE RUTINA N° 0{idx + 1}</Text>
                        <Text style={styles.routineTitle}>{rot.nombre.toUpperCase()}</Text>
                        <View style={styles.routineDaysRow}>
                          {rot.dias.map((d) => (
                            <View key={d} style={styles.dayBadge}>
                              <Text style={styles.dayBadgeText}>{d.toUpperCase()}</Text>
                            </View>
                          ))}
                        </View>
                      </View>

                      <View style={styles.routineActions}>
                        <TouchableOpacity
                          style={styles.editButton}
                          onPress={() => handleOpenEditRoutine(rot)}
                        >
                          <Ionicons name="pencil-sharp" size={16} color={COLORS.petroleum} />
                        </TouchableOpacity>

                        <TouchableOpacity
                          style={styles.deleteButton}
                          onPress={() => handleDeleteRoutine(rot.id, rot.nombre)}
                        >
                          <Ionicons name="trash-outline" size={16} color={COLORS.brickRed} />
                        </TouchableOpacity>
                      </View>
                    </View>

                    <View style={styles.routineExercisesContainer}>
                      <Text style={styles.routineSubheader}>
                        DESGLOSE DE MOVIMIENTOS ({rot.ejercicios.length}):
                      </Text>
                      {rot.ejercicios.map((exRef, eIdx) => {
                        const matchedEx = exercises.find((e) => e.id === exRef.exerciseId);
                        return (
                          <View key={eIdx} style={styles.routineExItem}>
                            <Text style={styles.routineExIndex}>0{eIdx + 1}.</Text>
                            <Text style={styles.routineExName}>
                              {matchedEx ? matchedEx.nombre.toUpperCase() : 'EJERCICIO ELIMINADO'}
                            </Text>
                            <Text style={styles.routineExMeta}>
                              {exRef.series}S x {exRef.repeticiones}R {exRef.peso ? `(${exRef.peso})` : ''}
                            </Text>
                          </View>
                        );
                      })}
                    </View>
                  </View>
                ))
              ) : (
                <View style={styles.emptyContainer}>
                  <Ionicons name="calendar-outline" size={36} color={COLORS.steelGray} />
                  <Text style={styles.emptyText}>NO HAY RUTINAS ASIGNADAS CON LOS FILTROS ACTIVOS.</Text>
                </View>
              )}
            </ScrollView>
          )}

          <AddExerciseModal
            visible={isAddExerciseVisible}
            exerciseToEdit={exerciseToEdit}
            onClose={() => {
              setIsAddExerciseVisible(false);
              setExerciseToEdit(null);
            }}
            onSave={handleSaveExercise}
          />

          <AddRoutineModal
            visible={isAddRoutineVisible}
            onClose={() => {
              setIsAddRoutineVisible(false);
              setRoutineToEdit(null);
            }}
            exercises={exercises}
            routines={routines}
            onSave={handleSaveRoutine}
            routineToEdit={routineToEdit}
          />
        </View>
      </SafeAreaView>
    </BackgroundWrapper>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    paddingHorizontal: 16,
    paddingTop: 8,
  },
  header: {
    marginBottom: 12,
  },
  tagCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 10,
    color: COLORS.steelGray,
    fontWeight: '700',
  },
  headerTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 34,
    fontWeight: '900',
    color: COLORS.softBlack,
    letterSpacing: 1.5,
  },
  headerSubtitle: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 12,
    fontWeight: '700',
    color: COLORS.petroleum,
  },
  segmentContainer: {
    flexDirection: 'row',
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 3,
    marginBottom: 14,
  },
  segmentButton: {
    flex: 1,
    paddingVertical: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  segmentButtonActive: {
    backgroundColor: COLORS.brickRed,
  },
  segmentText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 12,
    fontWeight: '800',
  },
  segmentTextActive: {
    color: COLORS.paper,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    marginTop: 10,
    fontSize: 12,
  },
  scrollContent: {
    paddingBottom: 30,
  },
  createBar: {
    marginBottom: 14,
  },
  searchContainer: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    paddingHorizontal: 12,
    paddingVertical: 8,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 12,
  },
  searchInput: {
    flex: 1,
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 13,
    fontWeight: '700',
  },
  filterSectionTitle: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 10,
    fontWeight: '800',
    color: COLORS.steelGray,
    marginBottom: 4,
    marginTop: 2,
  },
  filterScroll: {
    marginBottom: 12,
  },
  filterRow: {
    flexDirection: 'row',
    gap: 6,
  },
  filterChip: {
    backgroundColor: COLORS.paperCard,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
  },
  filterChipActive: {
    backgroundColor: COLORS.petroleum,
  },
  filterChipText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 11,
    fontWeight: '800',
  },
  filterChipTextActive: {
    color: COLORS.paper,
  },
  exerciseCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 10,
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
    gap: 12,
  },
  framedImageContainer: {
    width: 68,
    height: 68,
    backgroundColor: COLORS.paperDark,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    position: 'relative',
    overflow: 'hidden',
  },
  framedImage: {
    width: '100%',
    height: '100%',
  },
  imageCornerTag: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: COLORS.softBlack,
    paddingVertical: 1,
    alignItems: 'center',
  },
  imageCornerTagText: {
    fontFamily: FONTS.codeMono,
    fontSize: 8,
    color: COLORS.paper,
    fontWeight: '700',
  },
  cardInfo: {
    flex: 1,
  },
  cardCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
    fontWeight: '700',
  },
  cardTitle: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 18,
    fontWeight: '900',
    marginVertical: 2,
  },
  badgesRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  cardActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  actionButton: {
    padding: 6,
    borderWidth: 1,
    borderColor: COLORS.steelLight,
    backgroundColor: COLORS.paper,
  },
  deleteButton: {
    padding: 6,
  },
  emptyContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 30,
  },
  emptyText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 12,
    marginTop: 8,
    textAlign: 'center',
  },
  routineCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 14,
    marginBottom: 12,
  },
  routineHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  routineCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
    fontWeight: '700',
  },
  routineTitle: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 22,
    fontWeight: '900',
    marginTop: 2,
  },
  routineDaysRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 4,
    marginTop: 6,
  },
  dayBadge: {
    borderWidth: 1,
    borderColor: COLORS.brickRed,
    backgroundColor: COLORS.paper,
    paddingHorizontal: 6,
    paddingVertical: 2,
  },
  dayBadgeText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.brickRed,
    fontSize: 10,
    fontWeight: '900',
  },
  routineExercisesContainer: {
    marginTop: 12,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: COLORS.steelLight,
  },
  routineSubheader: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 11,
    fontWeight: '800',
    marginBottom: 6,
  },
  routineExItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 4,
  },
  routineExIndex: {
    fontFamily: FONTS.codeMono,
    color: COLORS.steelGray,
    fontSize: 10,
  },
  routineExName: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 13,
    fontWeight: '800',
    flex: 1,
  },
  routineExMeta: {
    fontFamily: FONTS.codeMono,
    color: COLORS.petroleum,
    fontSize: 10,
    fontWeight: '700',
  },
  routineActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  editButton: {
    borderWidth: 1.5,
    borderColor: COLORS.petroleum,
    padding: 6,
    backgroundColor: COLORS.paper,
  },
  deleteButton: {
    borderWidth: 1.5,
    borderColor: COLORS.brickRed,
    padding: 6,
    backgroundColor: COLORS.paper,
  },
});

