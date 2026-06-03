import React, { useEffect, useState } from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  View,
  Alert,
  ActivityIndicator,
  Platform,
} from 'react-native';
import axios from 'axios';

interface ScanResult {
  status: string;
  activeHosts: number;
  timestamp: string;
}

export default function App() {
  const [scanData, setScanData] = useState<ScanResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const performScan = async () => {
    setLoading(true);
    setError(null);
    try {
      // Example API call - replace with your actual endpoint
      const response = await axios.get('https://api.example.com/scan', {
        timeout: 10000,
      });
      setScanData(response.data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      setError(errorMessage);
      Alert.alert('Scan Error', errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#000" />
      <ScrollView contentInsetAdjustmentBehavior="automatic" style={styles.scrollView}>
        <View style={styles.header}>
          <Text style={styles.title}>Mac Scanner Pro</Text>
          <Text style={styles.subtitle}>UI ported from Macsen web app</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Network Scan</Text>
          {loading ? (
            <ActivityIndicator size="large" color="#0f0" style={styles.loader} />
          ) : scanData ? (
            <View style={styles.scanResults}>
              <Text style={styles.resultText}>Status: {scanData.status}</Text>
              <Text style={styles.resultText}>Active Hosts: {scanData.activeHosts}</Text>
              <Text style={styles.resultText}>Time: {scanData.timestamp}</Text>
            </View>
          ) : (
            <Text style={styles.placeholderText}>No scan data yet. Start a scan to begin.</Text>
          )}
          {error && <Text style={styles.errorText}>Error: {error}</Text>}
        </View>

        <View style={styles.infoCard}>
          <Text style={styles.infoTitle}>About</Text>
          <Text style={styles.infoText}>
            Mac Scanner Pro is a React Native application designed for network scanning and monitoring.
            {'\n\n'}
            Features:
            {'\n'}• Real-time network monitoring
            {'\n'}• Fast device detection
            {'\n'}• Beautiful UI
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
  },
  scrollView: {
    backgroundColor: '#000',
    padding: 16,
  },
  header: {
    marginBottom: 24,
    borderBottomWidth: 1,
    borderBottomColor: '#0f0',
    paddingBottom: 16,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#0f0',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
  },
  card: {
    backgroundColor: '#1a1a1a',
    borderWidth: 1,
    borderColor: '#0f0',
    borderRadius: 8,
    padding: 16,
    marginBottom: 16,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#0f0',
    marginBottom: 12,
  },
  loader: {
    marginVertical: 20,
  },
  scanResults: {
    paddingVertical: 8,
  },
  resultText: {
    color: '#0f0',
    fontSize: 14,
    marginVertical: 6,
    fontFamily: Platform.OS === 'ios' ? 'Courier New' : 'monospace',
  },
  placeholderText: {
    color: '#666',
    fontSize: 14,
    fontStyle: 'italic',
  },
  errorText: {
    color: '#ff4444',
    fontSize: 12,
    marginTop: 8,
  },
  infoCard: {
    backgroundColor: '#1a1a1a',
    borderWidth: 1,
    borderColor: '#333',
    borderRadius: 8,
    padding: 16,
  },
  infoTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#0f0',
    marginBottom: 8,
  },
  infoText: {
    color: '#ccc',
    fontSize: 13,
    lineHeight: 20,
  },
});
