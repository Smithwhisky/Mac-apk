import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

const Stack = createNativeStackNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen 
          name="Home" 
          component={HomeScreen} 
          options={{ title: 'Mac Scanner' }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

function HomeScreen() {
  return (
    <div style={{ padding: 20, backgroundColor: '#000', color: '#0f0', minHeight: '100vh' }}>
      <h1>Mac Scanner Pro</h1>
      <p>UI ported from Macsen web app</p>
      {/* Paste your Macsen UI components here */}
    </div>
  );
}