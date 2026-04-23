import React from 'react';
import { NavLink } from 'react-router-dom';
import '../styles/Navbar.css';
import { getAdminAccessState } from '../services/utils/adminAccess';

export default function Navbar() {
  const { isKnown, isAdmin } = getAdminAccessState();

  return (
    <header className="app-navbar">
      <div className="container is-app">
        <div className="is-flex is-justify-content-space-between is-align-items-center is-flex-wrap-wrap py-3 gap-3">
          <p className="title is-6 mb-0 brand-title">Course Prerequisite Visualizer</p>
          <nav className="buttons has-addons  nav-links" aria-label="Primary">
            <NavLink
              to="/"
              className={({ isActive }) => `button is-light ${isActive ? 'is-link is-selected' : ''}`}
            >
              DB Health Check
            </NavLink>
            {(!isKnown || isAdmin) ? (
              <NavLink
                to="/admin"
                className={({ isActive }) => `button is-light ${isActive ? 'is-link is-selected' : ''}`}
              >
                Admin
              </NavLink>
            ) : null}
            <NavLink
              to="/prereq-vis"
              className={({ isActive }) => `button is-light ${isActive ? 'is-link is-selected' : ''}`}
            >
              Course Prerequisites
            </NavLink>
          </nav>
        </div>
      </div>
    </header>
  );
}
