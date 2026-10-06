import * as FileSystem from 'expo-file-system/legacy';
import defaultQuotes from '../data/quotes.json';

const baseDir = FileSystem.documentDirectory || FileSystem.cacheDirectory || '';
const docDir = baseDir.endsWith('/') ? baseDir : baseDir + '/';

const EXERCISES_FILE = docDir + 'exercises.json';
const ROUTINES_FILE = docDir + 'routines.json';
const LOGS_FILE = docDir + 'workout_logs.json';
const QUOTES_FILE = docDir + 'quotes.json';

export const MUSCLES = [
  'Quads',
  'Femoral',
  'Gluteo',
  'Pantorilla',
  'Abductor',
  'Aductor',
  'Pecho',
  'Hombro',
  'Dorsales',
  'Espalda Alta',
  'Bicep',
  'Tricep',
  'Antebrazo',
];

export const TYPES = ['Pierna', 'Empuje', 'Espalda', 'Brazo'];

export const DAYS_OF_WEEK = [
  'Lunes',
  'Martes',
  'Miércoles',
  'Jueves',
  'Viernes',
  'Sábado',
  'Domingo',
];

export function getTipoFromMusculo(musculo) {
  switch (musculo) {
    case 'Quads':
    case 'Femoral':
    case 'Gluteo':
    case 'Pantorilla':
    case 'Pantorrilla':
    case 'Abductor':
    case 'Aductor':
      return 'Pierna';
    case 'Pecho':
    case 'Hombro':
      return 'Empuje';
    case 'Dorsales':
    case 'Espalda Alta':
      return 'Espalda';
    case 'Bicep':
    case 'Tricep':
    case 'Antebrazo':
      return 'Brazo';
    default:
      return 'Pierna';
  }
}

// Producción — sin datos de prueba
const SEED_EXERCISES = [];
const SEED_ROUTINES = [];
const SEED_LOGS = {};

// Helper to ensure file exists or populate with default data
async function ensureFileExists(filepath, defaultData) {
  try {
    const fileInfo = await FileSystem.getInfoAsync(filepath);
    if (!fileInfo.exists) {
      await FileSystem.writeAsStringAsync(filepath, JSON.stringify(defaultData, null, 2));
    }
  } catch (error) {
    console.error(`Error checking/creating file ${filepath}:`, error);
  }
}

// Get exercises
export async function getExercises() {
  try {
    await ensureFileExists(EXERCISES_FILE, SEED_EXERCISES);
    const content = await FileSystem.readAsStringAsync(EXERCISES_FILE);
    return JSON.parse(content);
  } catch (error) {
    console.error('Error reading exercises JSON:', error);
    return SEED_EXERCISES;
  }
}

// Save exercises
export async function saveExercises(exercises) {
  try {
    await FileSystem.writeAsStringAsync(EXERCISES_FILE, JSON.stringify(exercises, null, 2));
    return true;
  } catch (error) {
    console.error('Error saving exercises JSON:', error);
    return false;
  }
}

// Add Exercise
export async function addExercise({ nombre, musculo, imagen }) {
  const exercises = await getExercises();
  const tipo = getTipoFromMusculo(musculo);
  const newExercise = {
    id: 'ex_' + Date.now(),
    nombre: nombre.trim(),
    musculo,
    tipo,
    imagen: imagen || 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?q=80&w=400&auto=format&fit=crop',
    createdAt: new Date().toISOString(),
  };
  const updated = [newExercise, ...exercises];
  await saveExercises(updated);
  return newExercise;
}

// Update Exercise
export async function updateExercise(id, { nombre, musculo, imagen }) {
  const exercises = await getExercises();
  const tipo = getTipoFromMusculo(musculo);
  let updatedExercise = null;
  const updated = exercises.map((item) => {
    if (item.id === id) {
      updatedExercise = {
        ...item,
        nombre: nombre.trim(),
        musculo,
        tipo,
        imagen: imagen || item.imagen,
        updatedAt: new Date().toISOString(),
      };
      return updatedExercise;
    }
    return item;
  });
  await saveExercises(updated);
  return updatedExercise;
}

// Delete Exercise
export async function deleteExercise(id) {
  const exercises = await getExercises();
  const updated = exercises.filter((item) => item.id !== id);
  await saveExercises(updated);

  const routines = await getRoutines();
  const updatedRoutines = routines.map((rot) => ({
    ...rot,
    ejercicios: rot.ejercicios.filter((ex) => ex.exerciseId !== id),
  }));
  await saveRoutines(updatedRoutines);
  return true;
}

// Get Routines
export async function getRoutines() {
  try {
    await ensureFileExists(ROUTINES_FILE, SEED_ROUTINES);
    const content = await FileSystem.readAsStringAsync(ROUTINES_FILE);
    return JSON.parse(content);
  } catch (error) {
    console.error('Error reading routines JSON:', error);
    return SEED_ROUTINES;
  }
}

// Save Routines
export async function saveRoutines(routines) {
  try {
    await FileSystem.writeAsStringAsync(ROUTINES_FILE, JSON.stringify(routines, null, 2));
    return true;
  } catch (error) {
    console.error('Error saving routines JSON:', error);
    return false;
  }
}

// Add Routine
export async function addRoutine({ nombre, dias, ejercicios }) {
  const routines = await getRoutines();
  const newRoutine = {
    id: 'rot_' + Date.now(),
    nombre: nombre.trim(),
    dias: dias || [],
    ejercicios: ejercicios || [],
  };
  const updated = [newRoutine, ...routines];
  await saveRoutines(updated);
  return newRoutine;
}

// Delete Routine
export async function deleteRoutine(id) {
  const routines = await getRoutines();
  const updated = routines.filter((item) => item.id !== id);
  await saveRoutines(updated);
  return true;
}

// Update Routine
export async function updateRoutine(id, { nombre, dias, ejercicios }) {
  const routines = await getRoutines();
  const updated = routines.map((rot) => {
    if (rot.id === id) {
      return {
        ...rot,
        nombre: nombre.trim(),
        dias: dias || rot.dias,
        ejercicios: ejercicios || rot.ejercicios,
        updatedAt: new Date().toISOString(),
      };
    }
    return rot;
  });
  await saveRoutines(updated);
  return updated.find((r) => r.id === id);
}


// Update Weight in Routine
export async function updateExerciseWeightInRoutine(routineId, exerciseId, newWeight) {
  const routines = await getRoutines();
  const updatedRoutines = routines.map((rot) => {
    if (rot.id === routineId) {
      return {
        ...rot,
        ejercicios: rot.ejercicios.map((ex) =>
          ex.exerciseId === exerciseId ? { ...ex, peso: newWeight } : ex
        ),
      };
    }
    return rot;
  });
  await saveRoutines(updatedRoutines);
  return true;
}

// WORKOUT LOGS
export async function getWorkoutLogs() {
  try {
    await ensureFileExists(LOGS_FILE, SEED_LOGS);
    const content = await FileSystem.readAsStringAsync(LOGS_FILE);
    return JSON.parse(content);
  } catch (error) {
    console.error('Error reading workout logs JSON:', error);
    return SEED_LOGS;
  }
}

export async function getLogForDate(dateStr) {
  const allLogs = await getWorkoutLogs();
  return allLogs[dateStr] || { isSessionFinalized: false, sessionDuration: 0, exerciseLogs: {} };
}

export async function saveExerciseLog(dateStr, exerciseId, logData) {
  const allLogs = await getWorkoutLogs();
  if (!allLogs[dateStr]) {
    allLogs[dateStr] = { isSessionFinalized: false, sessionDuration: 0, exerciseLogs: {} };
  }
  allLogs[dateStr].exerciseLogs[exerciseId] = {
    ...logData,
    updatedAt: new Date().toISOString(),
  };

  try {
    await FileSystem.writeAsStringAsync(LOGS_FILE, JSON.stringify(allLogs, null, 2));
    return true;
  } catch (error) {
    console.error('Error saving workout log JSON:', error);
    return false;
  }
}

// Save Session Duration persistently
export async function saveSessionDuration(dateStr, sessionDuration) {
  const allLogs = await getWorkoutLogs();
  if (!allLogs[dateStr]) {
    allLogs[dateStr] = { isSessionFinalized: false, sessionDuration: 0, exerciseLogs: {} };
  }
  allLogs[dateStr].sessionDuration = sessionDuration;

  try {
    await FileSystem.writeAsStringAsync(LOGS_FILE, JSON.stringify(allLogs, null, 2));
    return true;
  } catch (error) {
    console.error('Error saving session duration JSON:', error);
    return false;
  }
}

// Finalize or Re-open session & save timer duration for a date (YYYY-MM-DD)
export async function toggleFinalizeSession(dateStr, isFinalized, sessionDuration) {
  const allLogs = await getWorkoutLogs();
  if (!allLogs[dateStr]) {
    allLogs[dateStr] = { isSessionFinalized: false, sessionDuration: 0, exerciseLogs: {} };
  }
  allLogs[dateStr].isSessionFinalized = isFinalized;
  if (sessionDuration !== undefined) {
    allLogs[dateStr].sessionDuration = sessionDuration;
  }
  allLogs[dateStr].finalizedAt = isFinalized ? new Date().toISOString() : null;

  try {
    await FileSystem.writeAsStringAsync(LOGS_FILE, JSON.stringify(allLogs, null, 2));
    return true;
  } catch (error) {
    console.error('Error toggling finalize session JSON:', error);
    return false;
  }
}

// MOTIVATIONAL QUOTES JSON PERSISTENCE
export async function getQuotes() {
  try {
    await FileSystem.writeAsStringAsync(QUOTES_FILE, JSON.stringify(defaultQuotes, null, 2));
    return defaultQuotes;
  } catch (error) {
    console.error('Error syncing quotes JSON:', error);
    return defaultQuotes;
  }
}
