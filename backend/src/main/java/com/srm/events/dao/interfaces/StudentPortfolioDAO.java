package com.srm.events.dao.interfaces;

import com.srm.events.analytics.StudentPortfolioData;
import com.srm.events.analytics.StudentPortfolioFilter;

public interface StudentPortfolioDAO {
    StudentPortfolioData load(Long studentId, StudentPortfolioFilter filter);
}
