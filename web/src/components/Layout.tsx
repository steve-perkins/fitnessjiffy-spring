import { useState, useRef, useEffect, ReactNode } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  AppBar,
  BottomNavigation,
  BottomNavigationAction,
  Box,
  Dialog,
  DialogContent,
  DialogTitle,
  Divider,
  Drawer,
  IconButton,
  Link,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import {
  Menu as MenuIcon,
  AccountCircle as AccountCircleIcon,
  Fastfood as FastfoodIcon,
  FitnessCenter as FitnessCenterIcon,
  Assessment as AssessmentIcon,
  Info as InfoIcon,
  Logout as LogoutIcon,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';

const DRAWER_WIDTH = 240;

const navItems = [
  { path: '/profile', label: 'Profile', icon: <AccountCircleIcon /> },
  { path: '/food', label: 'Food', icon: <FastfoodIcon /> },
  { path: '/exercise', label: 'Exercise', icon: <FitnessCenterIcon /> },
  { path: '/reports', label: 'Reports', icon: <AssessmentIcon /> },
];

interface LayoutProps {
  children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  const { logout } = useAuth();
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('sm'));
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [aboutOpen, setAboutOpen] = useState(false);
  const [appBarHeight, setAppBarHeight] = useState(0);
  const appBarRef = useRef<HTMLDivElement>(null);
  const location = useLocation();

  useEffect(() => {
    const updateAppBarHeight = () => {
      if (appBarRef.current) {
        setAppBarHeight(appBarRef.current.offsetHeight);
      }
    };

    updateAppBarHeight();
    window.addEventListener('resize', updateAppBarHeight);
    return () => window.removeEventListener('resize', updateAppBarHeight);
  }, []);

  const handleDrawerToggle = () => {
    setDrawerOpen(!drawerOpen);
  };

  const handleNavClick = () => {
    if (!isDesktop) {
      setDrawerOpen(false);
    }
  };

  // Get current nav index for bottom navigation
  const currentNavIndex = navItems.findIndex((item) => location.pathname.startsWith(item.path));

  const drawerContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <List sx={{ flexGrow: 1 }}>
        {navItems.map((item) => (
          <ListItem key={item.path} disablePadding>
            <ListItemButton
              component={NavLink}
              to={item.path}
              onClick={handleNavClick}
              selected={location.pathname.startsWith(item.path)}
              sx={{
                '&.active': {
                  backgroundColor: theme.palette.action.selected,
                },
              }}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Divider />
      <List>
        <ListItem disablePadding>
          <ListItemButton onClick={() => setAboutOpen(true)}>
            <ListItemIcon>
              <InfoIcon />
            </ListItemIcon>
            <ListItemText primary="About" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton onClick={logout}>
            <ListItemIcon>
              <LogoutIcon />
            </ListItemIcon>
            <ListItemText primary="Logout" />
          </ListItemButton>
        </ListItem>
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {/* App Bar */}
      <AppBar
        position="fixed"
        ref={appBarRef}
        sx={{ zIndex: theme.zIndex.drawer + 1 }}
      >
        <Toolbar>
          {!isDesktop && (
            <IconButton
              edge="start"
              color="inherit"
              aria-label="menu"
              onClick={handleDrawerToggle}
              sx={{ mr: 2 }}
            >
              <MenuIcon />
            </IconButton>
          )}
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Fitness Tracker
          </Typography>
        </Toolbar>
      </AppBar>

      {/* Spacer for fixed AppBar */}
      <Toolbar />

      {/* Sidebar Drawer */}
      <Drawer
        variant={isDesktop ? 'permanent' : 'temporary'}
        open={isDesktop ? true : drawerOpen}
        onClose={handleDrawerToggle}
        sx={{
          width: isDesktop ? DRAWER_WIDTH : 'auto',
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            marginTop: `${appBarHeight}px`,
            height: `calc(100% - ${appBarHeight}px)`,
            boxSizing: 'border-box',
          },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          ml: isDesktop ? `${DRAWER_WIDTH}px` : 0,
          mb: !isDesktop ? '56px' : 0, // Space for bottom nav on mobile
          width: isDesktop ? `calc(100% - ${DRAWER_WIDTH}px)` : '100%',
        }}
      >
        {children}
      </Box>

      {/* Bottom Navigation (Mobile Only) */}
      {!isDesktop && (
        <BottomNavigation
          value={currentNavIndex}
          showLabels
          sx={{
            position: 'fixed',
            bottom: 0,
            left: 0,
            right: 0,
            borderTop: 1,
            borderColor: 'divider',
          }}
        >
          {navItems.map((item) => (
            <BottomNavigationAction
              key={item.path}
              label={item.label}
              icon={item.icon}
              component={NavLink}
              to={item.path}
            />
          ))}
        </BottomNavigation>
      )}

      {/* About Dialog */}
      <Dialog open={aboutOpen} onClose={() => setAboutOpen(false)}>
        <DialogTitle>Fitness Tracker</DialogTitle>
        <DialogContent>
          <Typography paragraph>
            Copyright {new Date().getFullYear()}, Steve Perkins
          </Typography>
          <Link href="https://steveperkins.com" target="_blank" rel="noopener">
            steveperkins.com
          </Link>
        </DialogContent>
      </Dialog>
    </Box>
  );
}
