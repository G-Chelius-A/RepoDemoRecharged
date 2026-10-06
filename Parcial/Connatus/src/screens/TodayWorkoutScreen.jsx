import React, { useState, useEffect, useCallback } from 'react';
import {
  StyleSheet,
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Image,
  Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import {
  getRoutines,
  getExercises,
  getLogForDate,
  saveExerciseLog,
  saveSessionDuration,
  toggleFinalizeSession,
} from '../services/storageService';
import LogExerciseModal from '../components/LogExerciseModal';
import { COLORS, FONTS, GLOBAL_STYLES } from '../theme/theme';
import BackgroundWrapper from '../components/BackgroundWrapper';

export default function TodayWorkoutScreen() {
  const [loading, setLoading] = useState(true);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [weekDays, setWeekDays] = useState([]);

  const [routineForDay, setRoutineForDay] = useState(null);
  const [exerciseItems, setExerciseItems] = useState([]);
  const [dateLog, setDateLog] = useState({});
  const [isSessionFinalized, setIsSessionFinalized] = useState(false);

  // Live Timer
  const [activeTimer, setActiveTimer] = useState(false);
  const [timerSeconds, setTimerSeconds] = useState(0);

  // Selected exercise for modal
  const [logModalVisible, setLogModalVisible] = useState(false);
  const [selectedExForModal, setSelectedExForModal] = useState(null);

  useEffect(() => {
    let interval = null;
    if (activeTimer) {
      interval = setInterval(() => {
        setTimerSeconds((prev) => prev + 1);
      }, 1000);
    } else {
      clearInterval(interval);
    }
    return () => clearInterval(interval);
  }, [activeTimer]);

  const formatDateKey = (date) => {
    return date.toISOString().split('T')[0];
  };

  const handleToggleTimer = async () => {
    const nextState = !activeTimer;
    setActiveTimer(nextState);

    if (!nextState) {
      const dateKey = formatDateKey(selectedDate);
      await saveSessionDuration(dateKey, timerSeconds);
    }
  };

  const formatTimer = (totalSeconds) => {
    const hrs = Math.floor(totalSeconds / 3600);
    const mins = Math.floor((totalSeconds % 3600) / 60);
    const secs = totalSeconds % 60;
    const pad = (num) => String(num).padStart(2, '0');
    if (hrs > 0) {
      return `${pad(hrs)}:${pad(mins)}:${pad(secs)}`;
    }
    return `${pad(mins)}:${pad(secs)}`;
  };

  const resetTimer = async () => {
    setActiveTimer(false);
    setTimerSeconds(0);
    const dateKey = formatDateKey(selectedDate);
    await saveSessionDuration(dateKey, 0);
  };

  const calculateWeekDays = (baseDate) => {
    const current = new Date(baseDate);
    const dayOfWeek = current.getDay();
    const distanceToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1;

    const monday = new Date(current);
    monday.setDate(current.getDate() - distanceToMonday);

    const days = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(monday);
      d.setDate(monday.getDate() + i);
      days.push(d);
    }
    return days;
  };

  const getSpanishDayName = (date) => {
    const dayMap = ['DOMINGO', 'LUNES', 'MARTES', 'MIÉRCOLES', 'JUEVES', 'VIERNES', 'SÁBADO'];
    return dayMap[date.getDay()];
  };

  const loadDayData = async () => {
    setLoading(true);
    const dayName = getSpanishDayName(selectedDate);
    const dateKey = formatDateKey(selectedDate);

    const allRoutines = await getRoutines();
    const allExercises = await getExercises();
    const currentLog = await getLogForDate(dateKey);

    setDateLog(currentLog.exerciseLogs || {});
    setIsSessionFinalized(!!currentLog.isSessionFinalized);
    if (currentLog.sessionDuration && currentLog.sessionDuration > 0) {
      setTimerSeconds(currentLog.sessionDuration);
    } else {
      setTimerSeconds(0);
    }
    setActiveTimer(false);

    const foundRoutine = allRoutines.find((r) =>
      r.dias && r.dias.some(d => d.toUpperCase() === dayName)
    );

    if (foundRoutine) {
      setRoutineForDay(foundRoutine);
      const mapped = foundRoutine.ejercicios.map((exRef, idx) => {
        const fullEx = allExercises.find((e) => e.id === exRef.exerciseId);
        return {
          id: exRef.exerciseId || `ex_item_${idx}`,
          name: fullEx ? fullEx.nombre.toUpperCase() : 'EJERCICIO SIN NOMBRE',
          musculo: fullEx ? fullEx.musculo.toUpperCase() : 'VARIADO',
          tipo: fullEx ? fullEx.tipo.toUpperCase() : 'GENERAL',
          imagen: fullEx ? fullEx.imagen : 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?q=80&w=400&auto=format&fit=crop',
          series: `${exRef.series}`,
          reps: `${exRef.repeticiones}`,
          peso: exRef.peso || '',
        };
      });
      setExerciseItems(mapped);
    } else {
      setRoutineForDay(null);
      setExerciseItems([]);
    }

    setLoading(false);
  };

  useEffect(() => {
    setWeekDays(calculateWeekDays(selectedDate));
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadDayData();
    }, [selectedDate])
  );

  const changeWeek = (direction) => {
    const newDate = new Date(selectedDate);
    newDate.setDate(selectedDate.getDate() + direction * 7);
    setSelectedDate(newDate);
    setWeekDays(calculateWeekDays(newDate));
  };

  const handleToggleFinalize = async () => {
    const dateKey = formatDateKey(selectedDate);
    const newFinalizedState = !isSessionFinalized;

    if (newFinalizedState) {
      setActiveTimer(false);
      await toggleFinalizeSession(dateKey, true, timerSeconds);
      setIsSessionFinalized(true);
      Alert.alert(
        'SESIÓN REGISTRADA Y SELLADA',
        `La ficha ha sido archivada. Duración guardada: ${formatTimer(timerSeconds)}.`
      );
    } else {
      await toggleFinalizeSession(dateKey, false, timerSeconds);
      setIsSessionFinalized(false);
      Alert.alert(
        'FICHA REABIERTA',
        'La sesión está disponible para edición de cargas.'
      );
    }
  };

  const openCompleteModal = (exItem) => {
    if (isSessionFinalized) {
      Alert.alert('FICHA SELLADA', 'Reabre la sesión si necesitas modificar los datos registrados.');
      return;
    }
    setSelectedExForModal(exItem);
    setLogModalVisible(true);
  };

  const handleSaveExerciseLog = async (logData) => {
    const dateKey = formatDateKey(selectedDate);
    await saveExerciseLog(dateKey, selectedExForModal.id, logData);
    loadDayData();
  };

  const handleSkipExercise = async (exItem) => {
    if (isSessionFinalized) {
      Alert.alert('FICHA SELLADA', 'Reabre la sesión para modificar los ejercicios.');
      return;
    }
    const dateKey = formatDateKey(selectedDate);
    await saveExerciseLog(dateKey, exItem.id, {
      status: 'skipped',
      completedAt: new Date().toISOString(),
    });
    loadDayData();
  };

  const handleResetExercise = async (exItem) => {
    if (isSessionFinalized) {
      Alert.alert('FICHA SELLADA', 'Reabre la sesión para modificar los ejercicios.');
      return;
    }
    const dateKey = formatDateKey(selectedDate);
    await saveExerciseLog(dateKey, exItem.id, {
      status: 'pending',
    });
    loadDayData();
  };

  const completedCount = exerciseItems.filter(
    (e) => dateLog[e.id] && dateLog[e.id].status === 'completed'
  ).length;

  const skippedCount = exerciseItems.filter(
    (e) => dateLog[e.id] && dateLog[e.id].status === 'skipped'
  ).length;

  const progressPercent =
    exerciseItems.length > 0 ? Math.round((completedCount / exerciseItems.length) * 100) : 0;

  const isToday = formatDateKey(selectedDate) === formatDateKey(new Date());

  return (
    <BackgroundWrapper>
      <SafeAreaView style={styles.container}>
        <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
          {/* Editorial Top Header */}
          <View style={styles.header}>
            <Text style={styles.tagCode}>SESIÓN EN CURSO // {isToday ? 'DÍA ACTUAL' : 'HISTORIAL'}</Text>
            <Text style={styles.headerTitle}>HOY SE ENTRENA</Text>
            <Text style={styles.headerSub}>
              FECHA: {selectedDate.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' }).toUpperCase()}
            </Text>
          </View>

          {/* Technical Week Selector Sheet */}
          <View style={styles.weekNavCard}>
            <View style={styles.weekHeaderRow}>
              <TouchableOpacity style={styles.weekNavBtn} onPress={() => changeWeek(-1)}>
                <Ionicons name="chevron-back" size={16} color={COLORS.softBlack} />
                <Text style={styles.weekNavBtnText}>SEM. ANTERIOR</Text>
              </TouchableOpacity>

              <TouchableOpacity style={styles.todayChip} onPress={() => setSelectedDate(new Date())}>
                <Text style={styles.todayChipText}>[ HOY ]</Text>
              </TouchableOpacity>

              <TouchableOpacity style={styles.weekNavBtn} onPress={() => changeWeek(1)}>
                <Text style={styles.weekNavBtnText}>SEM. SIGUIENTE</Text>
                <Ionicons name="chevron-forward" size={16} color={COLORS.softBlack} />
              </TouchableOpacity>
            </View>

            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.daysScroll}>
              {weekDays.map((d) => {
                const dateKey = formatDateKey(d);
                const isSelected = formatDateKey(selectedDate) === dateKey;
                const isActualToday = formatDateKey(new Date()) === dateKey;
                const dayName = getSpanishDayName(d);

                return (
                  <TouchableOpacity
                    key={dateKey}
                    style={[
                      styles.dayChip,
                      isSelected && styles.dayChipActive,
                      isActualToday && !isSelected && styles.dayChipTodayHighlight,
                    ]}
                    onPress={() => setSelectedDate(d)}
                  >
                    <Text style={[styles.dayChipName, isSelected && styles.dayChipTextActive]}>
                      {dayName.substring(0, 3)}
                    </Text>
                    <Text style={[styles.dayChipNumber, isSelected && styles.dayChipTextActive]}>
                      {d.getDate()}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </ScrollView>
          </View>

          {loading ? (
            <View style={styles.loadingContainer}>
              <ActivityIndicator size="large" color={COLORS.brickRed} />
              <Text style={styles.loadingText}>CARGANDO REGISTROS DE FICHA...</Text>
            </View>
          ) : routineForDay ? (
            <>
              {/* Session Summary Card */}
              <View style={styles.progressCard}>
                <View style={styles.progressTopRow}>
                  <Text style={styles.routineBannerTitle}>{routineForDay.nombre.toUpperCase()}</Text>
                  {isSessionFinalized && (
                    <View style={styles.stampBadge}>
                      <Text style={styles.stampBadgeText}>SELLADO</Text>
                    </View>
                  )}
                </View>

                <View style={styles.progressHeader}>
                  <Text style={styles.progressTitle}>CUMPLIMIENTO DE FICHA</Text>
                  <Text style={styles.progressPercentage}>{progressPercent}%</Text>
                </View>

                <View style={styles.progressBarBg}>
                  <View style={[styles.progressBarFill, { width: `${progressPercent}%` }]} />
                </View>

                <Text style={styles.progressSubtext}>
                  {completedCount} REALIZADOS • {skippedCount} OMITIDOS • {exerciseItems.length} TOTALES
                </Text>
              </View>

              {/* Industrial Timer */}
              <View style={styles.timerBar}>
                <View style={styles.timerInfo}>
                  <Ionicons name="timer-outline" size={22} color={COLORS.petroleum} />
                  <View>
                    <Text style={styles.timerDisplayTitle}>
                      {isSessionFinalized ? 'DURACIÓN FINAL GUARDADA' : 'CRONÓMETRO DE TRABAJO'}
                    </Text>
                    <Text style={styles.timerDigits}>{formatTimer(timerSeconds)}</Text>
                  </View>
                </View>

                {!isSessionFinalized && (
                  <View style={styles.timerActions}>
                    <TouchableOpacity
                      style={[styles.timerButton, activeTimer && styles.timerButtonActive]}
                      onPress={handleToggleTimer}
                    >
                      <Ionicons name={activeTimer ? 'pause' : 'play'} size={14} color={COLORS.paper} />
                      <Text style={styles.timerButtonText}>
                        {activeTimer ? 'PAUSA' : 'INICIAR'}
                      </Text>
                    </TouchableOpacity>

                    <TouchableOpacity style={styles.timerResetBtn} onPress={resetTimer}>
                      <Ionicons name="refresh" size={14} color={COLORS.steelGray} />
                    </TouchableOpacity>
                  </View>
                )}
              </View>

              {/* Lock / Finalize Button */}
              <TouchableOpacity
                style={[
                  styles.finalizeBtn,
                  isSessionFinalized && styles.reopenBtn,
                ]}
                onPress={handleToggleFinalize}
              >
                <Ionicons
                  name={isSessionFinalized ? 'lock-open-outline' : 'shield-checkmark-outline'}
                  size={18}
                  color={isSessionFinalized ? COLORS.softBlack : COLORS.paper}
                />
                <Text
                  style={[
                    styles.finalizeBtnText,
                    isSessionFinalized && styles.reopenBtnText,
                  ]}
                >
                  {isSessionFinalized
                    ? 'DESBLOQUEAR FICHA PARA EDICIÓN'
                    : 'FINALIZAR Y SELLAR SESIÓN DE HOY'}
                </Text>
              </TouchableOpacity>

              {/* Exercise Technical Cards */}
              <View style={styles.sectionHeaderRow}>
                <Text style={GLOBAL_STYLES.sectionTitle}>EJERCICIOS ASIGNADOS ({exerciseItems.length})</Text>
              </View>

              {exerciseItems.map((item, index) => {
                const log = dateLog[item.id] || {};
                const isCompleted = log.status === 'completed';
                const isSkipped = log.status === 'skipped';

                return (
                  <View
                    key={item.id}
                    style={[
                      styles.exerciseCard,
                      isCompleted && styles.exerciseCardCompleted,
                      isSkipped && styles.exerciseCardSkipped,
                    ]}
                  >
                    <View style={styles.exCardMain}>
                      <TouchableOpacity
                        style={styles.checkTouch}
                        onPress={() => (isCompleted ? handleResetExercise(item) : openCompleteModal(item))}
                        disabled={isSessionFinalized}
                      >
                        <View style={[styles.checkBox, isCompleted && styles.checkBoxChecked, isSkipped && styles.checkBoxSkipped]}>
                          <Text style={styles.checkBoxText}>
                            {isCompleted ? '✓' : isSkipped ? '✕' : `${index + 1}`}
                          </Text>
                        </View>
                      </TouchableOpacity>

                      {/* CONATUS FRAMED EXERCISE IMAGE */}
                      <View style={styles.framedImageContainer}>
                        <Image source={{ uri: item.imagen }} style={styles.framedImage} resizeMode="cover" />
                        <View style={styles.imageCornerTag}>
                          <Text style={styles.imageCornerTagText}>MOV-0{index + 1}</Text>
                        </View>
                      </View>

                      <View style={styles.exerciseDetails}>
                        <Text style={styles.exerciseIndexCode}>{item.musculo}</Text>
                        <Text style={styles.exerciseName}>{item.name}</Text>
                        <Text style={styles.exerciseMeta}>
                          OBJETIVO: {item.series} SERIES x {item.reps} REPS {item.peso ? `(${item.peso})` : ''}
                        </Text>

                        {isCompleted && log.seriesReps && (
                          <View style={styles.logDetailBox}>
                            <Text style={styles.logDetailText}>
                              REGISTRADO: {log.pesoReal || '0'} KG — REPS: {log.seriesReps.map((s) => s.reps).join(' / ')}
                            </Text>
                          </View>
                        )}

                        {isSkipped && (
                          <View style={styles.skippedBadgeBox}>
                            <Text style={styles.skippedBadgeText}>[ EJERCICIO OMITIDO EN ESTA SESIÓN ]</Text>
                          </View>
                        )}
                      </View>
                    </View>

                    {/* Actions */}
                    {!isSessionFinalized ? (
                      <View style={styles.actionsRow}>
                        {!isCompleted && !isSkipped && (
                          <>
                            <TouchableOpacity
                              style={styles.btnComplete}
                              onPress={() => openCompleteModal(item)}
                            >
                              <Text style={styles.btnCompleteText}>+ ANOTAR PESO Y REPS</Text>
                            </TouchableOpacity>

                            <TouchableOpacity
                              style={styles.btnSkip}
                              onPress={() => handleSkipExercise(item)}
                            >
                              <Text style={styles.btnSkipText}>OMITIR</Text>
                            </TouchableOpacity>
                          </>
                        )}

                        {(isCompleted || isSkipped) && (
                          <TouchableOpacity
                            style={styles.btnReset}
                            onPress={() => handleResetExercise(item)}
                          >
                            <Ionicons name="refresh-outline" size={14} color={COLORS.steelGray} />
                            <Text style={styles.btnResetText}>REESTABLECER FICHA</Text>
                          </TouchableOpacity>
                        )}
                      </View>
                    ) : (
                      <View style={styles.lockedActionsRow}>
                        <Text style={styles.lockedActionsText}>FICHA ARCHIVADA Y SELLADA</Text>
                      </View>
                    )}
                  </View>
                );
              })}
            </>
          ) : (
            <View style={styles.noRoutineCard}>
              <Ionicons name="document-text-outline" size={40} color={COLORS.steelGray} />
              <Text style={styles.noRoutineTitle}>
                SIN FICHA ASIGNADA PARA {getSpanishDayName(selectedDate)}
              </Text>
              <Text style={styles.noRoutineSub}>
                ASIGNA UNA RUTINA A ESTE DÍA DESDE EL CATÁLOGO DE FICHAS O DISFRUTA DE TU DESCANSO.
              </Text>
            </View>
          )}

          <LogExerciseModal
            visible={logModalVisible}
            onClose={() => setLogModalVisible(false)}
            exerciseData={selectedExForModal}
            initialLog={selectedExForModal ? dateLog[selectedExForModal.id] : null}
            onSave={handleSaveExerciseLog}
          />
        </ScrollView>
      </SafeAreaView>
    </BackgroundWrapper>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    padding: 16,
    paddingBottom: 40,
  },
  header: {
    marginTop: 8,
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
  headerSub: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 12,
    fontWeight: '800',
    color: COLORS.brickRed,
    marginTop: 2,
  },
  weekNavCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 12,
    marginBottom: 16,
  },
  weekHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  weekNavBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  weekNavBtnText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.softBlack,
    fontSize: 11,
    fontWeight: '800',
  },
  todayChip: {
    borderWidth: 1,
    borderColor: COLORS.brickRed,
    paddingHorizontal: 8,
    paddingVertical: 2,
    backgroundColor: COLORS.paper,
  },
  todayChipText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.brickRed,
    fontSize: 11,
    fontWeight: '800',
  },
  daysScroll: {
    gap: 6,
  },
  dayChip: {
    backgroundColor: COLORS.paper,
    width: 44,
    height: 52,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dayChipActive: {
    backgroundColor: COLORS.brickRed,
    borderColor: COLORS.softBlack,
  },
  dayChipTodayHighlight: {
    borderColor: COLORS.brickRed,
  },
  dayChipName: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 10,
    fontWeight: '700',
  },
  dayChipNumber: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 16,
    fontWeight: '900',
  },
  dayChipTextActive: {
    color: COLORS.paper,
  },
  loadingContainer: {
    paddingVertical: 40,
    alignItems: 'center',
  },
  loadingText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    marginTop: 10,
    fontSize: 12,
  },
  progressCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 14,
    marginBottom: 14,
  },
  progressTopRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  routineBannerTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 22,
    fontWeight: '900',
    color: COLORS.softBlack,
  },
  stampBadge: {
    borderWidth: 1.5,
    borderColor: COLORS.brickRed,
    paddingHorizontal: 6,
    paddingVertical: 2,
    backgroundColor: COLORS.paper,
    transform: [{ rotate: '-30deg' }],
  },
  stampBadgeText: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 10,
    fontWeight: '900',
    color: COLORS.brickRed,
  },
  progressHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  progressTitle: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 11,
    fontWeight: '700',
  },
  progressPercentage: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.softBlack,
    fontSize: 16,
    fontWeight: '900',
  },
  progressBarBg: {
    height: 8,
    backgroundColor: COLORS.paperDark,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    overflow: 'hidden',
    marginBottom: 8,
  },
  progressBarFill: {
    height: '100%',
    backgroundColor: COLORS.brickRed,
  },
  progressSubtext: {
    fontFamily: FONTS.codeMono,
    color: COLORS.steelGray,
    fontSize: 9,
  },
  timerBar: {
    backgroundColor: COLORS.paperDark,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  timerInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  timerDisplayTitle: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.steelGray,
    fontSize: 10,
    fontWeight: '700',
  },
  timerDigits: {
    fontFamily: FONTS.metricCondensed,
    color: COLORS.petroleum,
    fontSize: 24,
    fontWeight: '900',
  },
  timerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  timerButton: {
    backgroundColor: COLORS.petroleum,
    paddingVertical: 6,
    paddingHorizontal: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  timerButtonActive: {
    backgroundColor: COLORS.brickRed,
  },
  timerButtonText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.paper,
    fontWeight: '800',
    fontSize: 12,
  },
  timerResetBtn: {
    padding: 6,
  },
  finalizeBtn: {
    backgroundColor: COLORS.brickRed,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    paddingVertical: 14,
    paddingHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginBottom: 18,
  },
  finalizeBtnText: {
    fontFamily: FONTS.titleCondensed,
    color: COLORS.paper,
    fontSize: 14,
    fontWeight: '900',
    letterSpacing: 1,
  },
  reopenBtn: {
    backgroundColor: COLORS.paper,
  },
  reopenBtnText: {
    color: COLORS.softBlack,
  },
  sectionHeaderRow: {
    marginBottom: 10,
  },
  exerciseCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 12,
    marginBottom: 12,
  },
  exerciseCardCompleted: {
    backgroundColor: COLORS.paperDark,
    borderColor: COLORS.petroleum,
  },
  exerciseCardSkipped: {
    opacity: 0.7,
  },
  exCardMain: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  checkTouch: {
    marginTop: 2,
  },
  checkBox: {
    width: 26,
    height: 26,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    backgroundColor: COLORS.paper,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkBoxChecked: {
    backgroundColor: COLORS.petroleum,
  },
  checkBoxSkipped: {
    backgroundColor: COLORS.brickRed,
  },
  checkBoxText: {
    fontFamily: FONTS.titleCondensed,
    fontWeight: '900',
    fontSize: 13,
    color: COLORS.softBlack,
  },
  framedImageContainer: {
    width: 58,
    height: 58,
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
    fontSize: 7,
    color: COLORS.paper,
    fontWeight: '700',
  },
  exerciseDetails: {
    flex: 1,
  },
  exerciseIndexCode: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
    fontWeight: '700',
  },
  exerciseName: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 18,
    fontWeight: '900',
    color: COLORS.softBlack,
    marginVertical: 1,
  },
  exerciseMeta: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 10,
    color: COLORS.steelGray,
    fontWeight: '700',
  },
  logDetailBox: {
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.petroleum,
    padding: 4,
    marginTop: 4,
  },
  logDetailText: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.petroleum,
    fontWeight: '700',
  },
  skippedBadgeBox: {
    marginTop: 4,
  },
  skippedBadgeText: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.brickRed,
    fontWeight: '700',
  },
  actionsRow: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 10,
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: COLORS.steelLight,
  },
  btnComplete: {
    backgroundColor: COLORS.brickRed,
    borderWidth: 1,
    borderColor: COLORS.softBlack,
    paddingVertical: 6,
    paddingHorizontal: 10,
  },
  btnCompleteText: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    fontWeight: '900',
    color: COLORS.paper,
  },
  btnSkip: {
    backgroundColor: COLORS.paper,
    borderWidth: 1,
    borderColor: COLORS.steelGray,
    paddingVertical: 6,
    paddingHorizontal: 10,
  },
  btnSkipText: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    fontWeight: '800',
    color: COLORS.steelGray,
  },
  btnReset: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  btnResetText: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 11,
    color: COLORS.steelGray,
    fontWeight: '700',
  },
  lockedActionsRow: {
    marginTop: 8,
    paddingTop: 6,
    borderTopWidth: 1,
    borderTopColor: COLORS.steelLight,
  },
  lockedActionsText: {
    fontFamily: FONTS.codeMono,
    fontSize: 9,
    color: COLORS.steelGray,
  },
  noRoutineCard: {
    backgroundColor: COLORS.paperCard,
    borderWidth: 1.5,
    borderColor: COLORS.softBlack,
    padding: 24,
    alignItems: 'center',
    marginVertical: 20,
  },
  noRoutineTitle: {
    fontFamily: FONTS.metricCondensed,
    fontSize: 20,
    fontWeight: '900',
    color: COLORS.softBlack,
    marginTop: 12,
    textAlign: 'center',
  },
  noRoutineSub: {
    fontFamily: FONTS.titleCondensed,
    fontSize: 12,
    color: COLORS.steelGray,
    textAlign: 'center',
    marginTop: 6,
    lineHeight: 18,
  },
});
