package com.ppp.grade.subject.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.ppp.grade.graduation.persistence.GraduationDAO;
import com.ppp.grade.graduation.persistence.GraduationVO;
import com.ppp.grade.minortable.persistence.MinortableDAO;
import com.ppp.grade.minortable.persistence.MinortableVO;
import com.ppp.grade.select.persistence.SelectDAO;
import com.ppp.grade.select.persistence.SelectVO;
import com.ppp.grade.subject.persistence.SubjectDAO;
import com.ppp.grade.subject.persistence.SubjectVO;
import com.ppp.grade.user.controller.LoginController;

public class SubjectSideController implements Controller {
	ModelAndView mav = new ModelAndView();

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		
		//전공 변수
				int majorSum = 0;
				//학과기초 변수
				int majorbaseSum = 0;
				//일반교양 변수
				int generalSum1 = 0;
				//필수교양 변수
				int generalSum2 = 0;
				//부전공용 계산 변수
				int minorSum = 0;
				//남은학점 계산 변수
				int excessSum =0;
						
			SelectVO selectVO = new SelectVO();
			SelectDAO selectDAO = new SelectDAO();
			List<SelectVO> MatchingSelectList = new ArrayList<SelectVO>();
			List<SubjectVO> subjectList = new ArrayList<SubjectVO>();
			
			String studentNum = LoginController.getStu_아이디();
			MatchingSelectList = selectDAO.getSubjectList(studentNum);
			String[] str = new String[MatchingSelectList.size()];
			int i =0;
			//리스트 to 배열
			for (SelectVO obj :  MatchingSelectList){
				str[i++] = obj.get과목코드();
			}
			//과목리스트 가져오기
			SubjectDAO subjectDAO = new SubjectDAO();
			subjectList = subjectDAO.getSubjectWithSubjectNum(str);
			
			// 이거 2에서 쓰이는 용도인 학과코드 추출
			String MajorNum = subjectList.get(0).get학과코드();

			for (SubjectVO obj : subjectList) {
				if (obj.get구분().equals("일반교양")) {
		            generalSum1 += Integer.parseInt(obj.get학점());
		         }
		         else if((obj.get구분().equals("필수교양"))){
		            generalSum2 += Integer.parseInt(obj.get학점());
		         }
		         else if(obj.get학과코드().equals(MajorNum)&&obj.get구분().equals("전공")){
		            majorSum += Integer.parseInt(obj.get학점());
		         }
		         else if(obj.get구분().equals("학과기초") && obj.get학과코드().equals(MajorNum)){
		            majorbaseSum +=Integer.parseInt(obj.get학점());
		         }
		         else{
		            minorSum += Integer.parseInt(obj.get학점());
		         }
			}
			System.out.println("주전공 =" +majorSum);
			System.out.println("부전공 = "+minorSum);

			// 2. GRADUATION 정보가져오기(학과코드로)
			
			String MinorNum = LoginController.getStu_복수전공();
			System.out.println(MinorNum);
			
			//복수전공이 없으면 즉, 전공만 듣는 학생 계산
			if(MinorNum==null){
				GraduationDAO graduationDAO = new GraduationDAO();
		
				GraduationVO graduation = new GraduationVO();
				graduation = graduationDAO.getGraduationWithMajorNum(MajorNum);
		
				System.out.println(graduation.get전공심화());
				System.out.println(graduation.get일반교양());
		
				// 3. 연산
				 //전공점수에 대한 +로 만들어주는 부분
		         if((Integer.parseInt(graduation.get전공심화())<majorSum)){
						excessSum = majorSum - (Integer.parseInt(graduation.get전공심화()));
						mav.addObject("Majorresult", "+"+excessSum);
					}else{
						mav.addObject("Majorresult", (Integer.parseInt(graduation.get전공심화()) - majorSum)+"");
					}
		       //일반교양에 대한 +로 만들어주는 부분
		         if((Integer.parseInt(graduation.get일반교양())<generalSum1)){
						excessSum = generalSum1 - (Integer.parseInt(graduation.get일반교양()));
						mav.addObject("Generalresult1", "+"+excessSum);
					}else{
						mav.addObject("Generalresult1", (Integer.parseInt(graduation.get일반교양()) - generalSum1)+"");
					}
		         //필수교양에 대한 +로 만들어주는 부분
		         if((Integer.parseInt(graduation.get필수교양())<generalSum2)){
						excessSum = generalSum2 - (Integer.parseInt(graduation.get필수교양()));
						mav.addObject("Generalresult2", "+"+excessSum);
					}else{
						mav.addObject("Generalresult2", (Integer.parseInt(graduation.get필수교양()) - generalSum2)+"");
					}
		         //학과기초에 대한 +로 만들어주는 부분
		         if((Integer.parseInt(graduation.get학과기초())<majorbaseSum)){
						excessSum = majorbaseSum - (Integer.parseInt(graduation.get학과기초()));
						mav.addObject("Majorbaseresult", "+"+excessSum);
					}else{
						mav.addObject("Majorbaseresult", (Integer.parseInt(graduation.get학과기초()) - majorbaseSum)+"");
					}
				mav.addObject("MatchingSelectList",subjectList);
				mav.addObject("MajorSum", majorSum+"");
				mav.addObject("GeneralSum1", generalSum1+"");
				mav.addObject("GeneralSum2", generalSum2+"");
				mav.addObject("MajorbaseSum", majorbaseSum+"");
				mav.setViewName("result.jsp");
				
			
				return mav;
			}
			else{
				GraduationDAO graduationDAO = new GraduationDAO();
				MinortableDAO minortableDAO = new MinortableDAO();
				
				GraduationVO graduation = new GraduationVO();
				MinortableVO minortable = new MinortableVO();
				graduation = graduationDAO.getGraduationWithMajorNum(MajorNum);
				minortable = minortableDAO.getMinortableWithMinorNum(MinorNum);
		
				System.out.println("전공학점 = "+minortable.get전공학점());
				System.out.println("복수전공학점 = "+minortable.get복수전공학점());
				System.out.println("일반교양 = "+graduation.get일반교양());
		
				// 3. 연산
				//전공점수에 대한 +로 만들어주는 부분
		         if((Integer.parseInt(minortable.get전공학점())<majorSum)){
						excessSum = majorSum - (Integer.parseInt(minortable.get전공학점()));
						mav.addObject("Majorresult", "+"+excessSum);
					}else{
						mav.addObject("Majorresult", (Integer.parseInt(minortable.get전공학점()) - majorSum)+"");
					}
		       //복수전공에 대한 +로 만들어주는 부분
		         if((Integer.parseInt(minortable.get복수전공학점())<minorSum)){
						excessSum = minorSum - (Integer.parseInt(minortable.get복수전공학점()));
						mav.addObject("Minorresult", "+"+excessSum);
					}else{
						mav.addObject("Minorresult", (Integer.parseInt(minortable.get복수전공학점()) - minorSum)+"");
					}
		       //일반교양에 대한 +로 만들어주는 부분
		         if((Integer.parseInt(graduation.get일반교양())<generalSum1)){
						excessSum = generalSum1 - (Integer.parseInt(graduation.get일반교양()));
						mav.addObject("Generalresult1", "+"+excessSum);
					}else{
						mav.addObject("Generalresult1", (Integer.parseInt(graduation.get일반교양()) - generalSum1)+"");
					}
		         //필수교양에 대한 +로 만들어주는 부분
		         if((Integer.parseInt(graduation.get필수교양())<generalSum2)){
						excessSum = generalSum2 - (Integer.parseInt(graduation.get필수교양()));
						mav.addObject("Generalresult2", "+"+excessSum);
					}else{
						mav.addObject("Generalresult2", (Integer.parseInt(graduation.get필수교양()) - generalSum2)+"");
					}
		         //학과기초에 대한 +로 만들어주는 부분
		         if((Integer.parseInt(graduation.get학과기초())<majorbaseSum)){
						excessSum = majorbaseSum - (Integer.parseInt(graduation.get학과기초()));
						mav.addObject("Majorbaseresult", "+"+excessSum);
					}else{
						mav.addObject("Majorbaseresult", (Integer.parseInt(graduation.get학과기초()) - majorbaseSum)+"");
					}
				//현재까지 들은 학점을 보여주기위한 값들........
		         mav.addObject("MatchingSelectList", subjectList);
				mav.addObject("MajorSum", majorSum+"");
				mav.addObject("MinorSum",  minorSum+"");
				mav.addObject("GeneralSum1", generalSum1+"");
				mav.addObject("GeneralSum2", generalSum2+"");
				mav.addObject("MajorbaseSum", majorbaseSum+"");
				mav.setViewName("result.jsp");
				
			
				return mav;
			
			
			
			
			
	
		}
	}
}
