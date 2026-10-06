import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';
import { View, StyleSheet, Platform, Text } from 'react-native';

import HomeScreen from '../screens/HomeScreen';
import TodayWorkoutScreen from '../screens/TodayWorkoutScreen';
import StatsScreen from '../screens/StatsScreen';
import ExercisesScreen from '../screens/ExercisesScreen';
import { COLORS, FONTS } from '../theme/theme';

const Tab = createBottomTabNavigator();

export default function TabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarShowLabel: true,
        tabBarActiveTintColor: COLORS.brickRed,
        tabBarInactiveTintColor: COLORS.steelGray,
        tabBarStyle: {
          backgroundColor: COLORS.paper,
          borderTopColor: COLORS.softBlack,
          borderTopWidth: 2,
          height: Platform.OS === 'ios' ? 88 : 68,
          paddingBottom: Platform.OS === 'ios' ? 24 : 10,
          paddingTop: 8,
          elevation: 0,
        },
        tabBarLabelStyle: {
          fontFamily: FONTS.titleCondensed,
          fontSize: 11,
          fontWeight: '800',
          textTransform: 'uppercase',
          letterSpacing: 0.8,
          marginTop: 2,
        },
        tabBarIcon: ({ focused, color, size }) => {
          let iconName;

          if (route.name === 'Inicio') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'Hoy se entrena') {
            iconName = focused ? 'fitness' : 'fitness-outline';
          } else if (route.name === 'Estadísticas') {
            iconName = focused ? 'analytics' : 'analytics-outline';
          } else if (route.name === 'Ejercicios') {
            iconName = focused ? 'library' : 'library-outline';
          }

          return (
            <View style={[styles.iconBox, focused && styles.iconBoxActive]}>
              <Ionicons name={iconName} size={20} color={color} />
            </View>
          );
        },
      })}
    >
      <Tab.Screen 
        name="Inicio" 
        component={HomeScreen} 
        options={{ tabBarLabel: '01. INICIO' }}
      />
      <Tab.Screen 
        name="Hoy se entrena" 
        component={TodayWorkoutScreen} 
        options={{ tabBarLabel: '02. SESIÓN' }}
      />
      <Tab.Screen 
        name="Estadísticas" 
        component={StatsScreen} 
        options={{ tabBarLabel: '03. REGISTRO' }}
      />
      <Tab.Screen 
        name="Ejercicios" 
        component={ExercisesScreen} 
        options={{ tabBarLabel: '04. FICHAS' }}
      />
    </Tab.Navigator>
  );
}

const styles = StyleSheet.create({
  iconBox: {
    padding: 2,
  },
  iconBoxActive: {
    borderBottomWidth: 2,
    borderBottomColor: COLORS.brickRed,
  },
});
