<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.androidprojecttemplate.views.Nav">
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navView"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:headerLayout="@menu/nav_header"
        app:menu="@menu/nav_menu">
    </com.google.android.material.navigation.NavigationView>

    <androidx.appcompat.widget.Toolbar
        android:id="@+id/nav_toolbar"
        android:layout_width="603dp"
        android:layout_height="wrap_content"
        android:background="?attr/colorPrimary"
        android:minHeight="?attr/actionBarSize"
        android:theme="?attr/actionBarTheme" />
</RelativeLayout>


