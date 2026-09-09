import React from 'react';
import { createDrawerNavigator } from '@react-navigation/drawer';
import SplashScreen from '../screens/SplashScreen';
import HomeScreen from '../screens/HomeScreen';
import DiceScreen from '../screens/DiceScreen';
import MemoryScreen from '../screens/MemoryScreen';
import CurrencyScreen from '../screens/CurrencyScreen';
import IMCScreen from '../screens/IMCScreen';
import Logo from '../components/Logo';

const Drawer = createDrawerNavigator();

export default function DrawerNavigator() {
  return (
    <Drawer.Navigator
      initialRouteName="Splash"
      screenOptions={{
        headerStyle: { backgroundColor: '#182848' },
        headerTintColor: '#fff',
        drawerActiveTintColor: '#182848',
        drawerLabelStyle: { fontSize: 16 }
      }}
    >
      <Drawer.Screen
        name="Splash"
        component={SplashScreen}
        options={{ headerShown: false, drawerItemStyle: { display: 'none' } }}
      />
      <Drawer.Screen
        name="Home"
        component={HomeScreen}
        options={{
          title: 'Inicio',
          drawerIcon: () => <Logo size="small" />
        }}
      />
      <Drawer.Screen
        name="Dados"
        component={DiceScreen}
        options={{ title: '🎲 Dados' }}
      />
      <Drawer.Screen
        name="Memorama"
        component={MemoryScreen}
        options={{ title: '🧠 Memorama' }}
      />
      <Drawer.Screen
        name="Divisas"
        component={CurrencyScreen}
        options={{ title: '💱 Calculadora de divisas' }}
      />
      <Drawer.Screen
        name="IMC"
        component={IMCScreen}
        options={{ title: '⚖️ Calculadora IMC' }}
      />
    </Drawer.Navigator>
  );
}
