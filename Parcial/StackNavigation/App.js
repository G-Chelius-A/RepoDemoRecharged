import * as React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import HomeScreen from './components/HomeScreen';
import ImcScreen from './src/screens/ImcScreen';
import TipScreen from './components/TipScreen';
import CurrencyScreen from './src/screens/CurrencyScreen';

const Stack = createNativeStackNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Home">
        <Stack.Screen name="Home" component={HomeScreen} options={{title: "Menú principal"}} />
        <Stack.Screen name="IMC" component={ImcScreen} options={{title: "Calculadora IMC"}} />
        <Stack.Screen name="Currency" component={CurrencyScreen} options={{title: "Conversor Divisas"}} />
        <Stack.Screen name="Tip" component={TipScreen} options={{title: "Cálculo Propina"}} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
