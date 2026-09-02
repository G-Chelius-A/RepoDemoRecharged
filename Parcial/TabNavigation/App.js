import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import ProfileScreen from './src/screens/ProfileScreen';
import HomeScreen from './src/screens/HomeScreen';
import SearchScreen from './src/screens/SearchScreen';


const Tab = createBottomTabNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Tab.Navigator screenOptions={{headerShown: false, tabBarActiveTintColor:'red', tabBarInactiveTintColor:'gray'}}>
        <Tab.Screen name="Inicio" component={HomeScreen}/>
        <Tab.Screen name="Buscar" component={SearchScreen}/>
        <Tab.Screen name="Perfil" component={ProfileScreen}/>
      </Tab.Navigator>
    </NavigationContainer>
  );
}