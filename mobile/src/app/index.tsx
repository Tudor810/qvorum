import { colors, fontSize, spacing, strings } from '@qvorum/shared';
import { StyleSheet, Text, View } from 'react-native';
import { LoginForm } from '@/features/auth';

export default function Index() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{strings.app.name}</Text>
      <LoginForm />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.xl,
    padding: spacing.xl,
    backgroundColor: colors.background,
  },
  title: {
    fontSize: fontSize.xxl,
    color: colors.text,
  },
});
