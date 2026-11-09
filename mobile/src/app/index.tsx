import { strings } from '@qvorum/shared';
import { View } from 'react-native';
import { AppText } from '@/components/ui';
import { LoginForm } from '@/features/auth';
import { makeStyles } from '@/lib/theme';

export default function Index() {
  const styles = useStyles();
  return (
    <View style={styles.container}>
      <AppText variant="display" color="primary">
        {strings.app.name}
      </AppText>
      <LoginForm />
    </View>
  );
}

const useStyles = makeStyles((t) => ({
  container: {
    flex: 1,
    justifyContent: 'center',
    gap: t.spacing.xl,
    padding: t.spacing.xl,
    backgroundColor: t.colors.background,
  },
}));
