import { Component, NgZone, AfterViewInit, OnDestroy, OnInit } from '@angular/core';
import * as am4core from "@amcharts/amcharts4/core";
import * as am4charts from "@amcharts/amcharts4/charts";
import am4themes_animated from "@amcharts/amcharts4/themes/animated";
import { OrgadminDashboardService } from 'src/app/services/orgadmin-dashboard.service';
import Swal from 'sweetalert2';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import {ModalDismissReasons, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import { OrgadminService } from 'src/app/services/orgadmin.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Router } from '@angular/router';
import { ReportDeliveryDetailsComponent } from '../report-delivery-details/report-delivery-details.component';
import _ from 'lodash';
am4core.useTheme(am4themes_animated);
@Component({
  selector: 'app-orgadmin-uploaddetails',
  templateUrl: './orgadmin-uploaddetails.component.html',
  styleUrls: ['./orgadmin-uploaddetails.component.scss']
})
export class OrgadminUploaddetailsComponent implements OnInit, OnDestroy,AfterViewInit {
 private chart: am4charts.XYChart | undefined;
 getuploadinfo: any=[];
 statuscodes: any;
 getChartData: any=[];
 ChartDataListing: any=[];
 getStatCodes: any;
 tmp: any=[];
 getCandidate: any=[];
 currentPage = 1;
 pageSize: number = 10;
 currentPageIndex: number = 0;
 searchText: string = '';
 startdownload:boolean=false;


 public stat_INVITATIONSENT = true;
 public stat_NEWUPLOAD = true;
 public stat_btn_SendInvi = true;
 public stat_btn_ReInvite = true;
 public stat_btn_QC = true;
 stat_linkAdminApproval:boolean = false;


 containerStat:boolean = false;
 isCBadmin:boolean = false;
 getRolePerMissionCodes:any=[];
 EDITCANDIDATE_stat:boolean = false;
 showContactNumberError:boolean =false;
 showEmailIdError:boolean = false;
 formSendInvitation = new FormGroup({
  candidateReferenceNo: new FormControl('', Validators.required),
  statuscode: new FormControl('', Validators.required)
});

updateCandidate = new FormGroup({
  applicantId: new FormControl(''),
  candidateName: new FormControl('', Validators.required),
  createdByUserFirstName: new FormControl('', Validators.required),
  candidateCode: new FormControl('', Validators.required),
  contactNumber:  new FormControl('', [Validators.minLength(10), Validators.maxLength(10), Validators.pattern('[6-9]\\d{9}')]),
  emailId: new FormControl('', [Validators.required,Validators.email])
});

patchUserValues() {
  this.formSendInvitation.patchValue({
    candidateReferenceNo: this.tmp,
    statuscode: "INVITATIONSENT",
  });
}

reInvitePatchValues() {
  this.formSendInvitation.patchValue({
    candidateReferenceNo: this.tmp,
    statuscode: "REINVITE",
  });
}

  constructor(private zone: NgZone, private orgadmin:OrgadminDashboardService, private modalService: NgbModal,
    private router: Router, private orgadminservice: OrgadminService, public authService: AuthenticationService) {
    this.getStatCodes = this.orgadmin.getStatusCode();
    if(this.getStatCodes){
      var userId:any = this.authService.getuserId();
      var fromDate:any = localStorage.getItem('dbFromDate');
      var toDate:any = localStorage.getItem('dbToDate');
      let filterData = {
        'userId': userId,
        'fromDate': fromDate,
        'toDate': toDate,
        'status': this.getStatCodes,
        //adding below parameter to get the backend pagination list
        'pageNumber':this.currentPageIndex
      }
      this.orgadmin.getChartDetails(filterData).subscribe((data: any)=>{
        this.ChartDataListing=data.data.candidateDtoList;
        // console.log(this.ChartDataListing);
        //this.ChartDataListing.reverse();
        console.log("After : ", this.ChartDataListing)
        //console.log(data);
        const startIndex = this.currentPageIndex * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        return this.ChartDataListing.slice(startIndex, endIndex);
      });

    }


  }

  downloadReports(candidate: any, reportType: any) {
    if(reportType == 'Final Report') {
      for(let i=0; i<this.ChartDataListing.length; i++) {
        let index = _.findIndex(this.ChartDataListing[i].contentDTOList, {contentSubCategory: 'FINAL'});
        this.ChartDataListing[i].pre_approval_content_id = (index != -1) ? this.ChartDataListing[i].contentDTOList[index].contentId : -1;
        console.log('Lavanyafinal',index)
        this.startdownload=true

      }
    } else if(reportType == 'Interim Report') {
      for(let i=0; i<this.ChartDataListing.length; i++) {
        let index = this.ChartDataListing[i].contentDTOList.findIndex((item: any)=> item.path.includes('INTERIM.pdf'));
        this.ChartDataListing[i].pre_approval_content_id = (index != -1) ? this.ChartDataListing[i].contentDTOList[index].contentId : -1;
        console.log('interim',index)
        this.startdownload=true

      }
    } else if(reportType == 'QC Pending'){
      for(let i=0; i<this.ChartDataListing.length; i++) {
        let index = _.findIndex(this.ChartDataListing[i].contentDTOList, {contentSubCategory: 'PRE_APPROVAL'});
        this.ChartDataListing[i].pre_approval_content_id = (index != -1) ? this.ChartDataListing[i].contentDTOList[index].contentId : -1;
        console.log('Lavanyafinal',index)
        this.startdownload=true
      }
    }

    if(this.startdownload==true){
      if(candidate.pre_approval_content_id != -1) {
        console.log(candidate,"-----if--------");
        this.orgadmin.getSignedURLForContent(candidate.pre_approval_content_id).subscribe((url: any)=>{
          // window.location.href = url;
          window.open(url.data, '_blank');
        });
      }
    }
  }

  performSearch(){
    console.log('Search Text:', this.searchText);
    const username = this.authService.getuserName();
    const userID = this.authService.getuserId();
    const orgId = this.authService.getOrgID();
    const role = this.authService.getRoles();
    const userRoleName = this.authService.getroleName();
    console.warn("username:::",username);
    const searchData = {
      userSearchInput: this.searchText,
      agentName: username,
      organisationId:orgId,
      roleName:userRoleName,
      userId:userID
    };
    console.log('Search Data:', searchData);

    this.orgadmin.getAllSearchData(searchData).subscribe((data:any)=>{
      this.ChartDataListing=data.data.candidateDtoList;
      console.warn("data",data);
    })

  }

  ngOnInit(): void {
    const isCBadminVal = this.authService.getRoles();
    if(this.getStatCodes){
      $(".orgadmin_uploaddetails").addClass(this.getStatCodes);
      if(this.getStatCodes === "NEWUPLOAD"){
        $(".dbtabheading").text("New Upload");
        this.stat_NEWUPLOAD = false;
        this.stat_btn_ReInvite = true;
        this.stat_INVITATIONSENT = true;
        this.stat_btn_SendInvi = false;
        this.stat_linkAdminApproval = true;

        // this.stat_btn_ReInvite = false;
        // this.stat_INVITATIONSENT = false;
        // this.stat_btn_SendInvi = false;
      }else if(this.getStatCodes === "INVITATIONSENT"){
        $(".dbtabheading").text("Invitation Sent");
        this.stat_INVITATIONSENT = true;
        this.stat_btn_SendInvi = false;
        this.stat_btn_ReInvite = true;
      }else if(this.getStatCodes === "INVITATIONEXPIRED"){
        $(".dbtabheading").text("Invitation Expired");
        this.stat_btn_SendInvi = false;
      }else if(this.getStatCodes === "INVALIDUPLOAD"){
        $(".dbtabheading").text("Invalid Upload");
        this.stat_btn_SendInvi = false;
      }else if(this.getStatCodes === "REINVITE"){
        $(".dbtabheading").text("Re Invite");
        this.stat_NEWUPLOAD = false;
        this.stat_btn_ReInvite = false;
        this.stat_INVITATIONSENT = false;
        this.stat_btn_SendInvi = false;
            }
      this.containerStat = true;
      //isCBadmin required for drilldown dashboard at Superadmin
      if(isCBadminVal == '"ROLE_CBADMIN"'){
        this.isCBadmin = true;
        this.stat_INVITATIONSENT = false;
        this.stat_btn_SendInvi = false;
        this.stat_btn_ReInvite = false;
        this.stat_linkAdminApproval = false;

      }else{
        this.isCBadmin = false;
      }
      //console.log(isCBadminVal);
      //console.log(this.isCBadmin);
      }



//Role Management
this.orgadminservice.getRolePerMissionCodes(this.authService.getRoles()).subscribe(
  (result:any) => {
  this.getRolePerMissionCodes = result.data;
    //console.log(this.getRolePerMissionCodes);
    if(this.getRolePerMissionCodes){
      if(this.getRolePerMissionCodes.includes("EDITCANDIDATE")){
        this.EDITCANDIDATE_stat = true;
      }


    }
});

  }
  ngAfterViewInit() {
    setTimeout(() =>{
      this.ngOnDestroy();
      this.loadCharts();
    },50);
  }

  linkAdminApproval(candidateCode:any) {
    const billUrl = 'admin/cReportApproval/'+[candidateCode];
    this.router.navigate([billUrl]);
  }

  loadCharts(){
    this.zone.runOutsideAngular(() => {
      let chart = am4core.create("chartdiv", am4charts.PieChart);
      chart.innerRadius = am4core.percent(50);
      chart.legend = new am4charts.Legend();

      chart.legend.itemContainers.template.paddingTop = 4;
      chart.legend.itemContainers.template.paddingBottom = 4;
      chart.legend.fontSize = 13;
      chart.legend.useDefaultMarker = true;
      let marker:any = chart.legend.markers.template.children.getIndex(0);
      marker.cornerRadius(12, 12, 12, 12);
      marker.strokeWidth = 3;
      marker.strokeOpacity = 1;
      marker.stroke = am4core.color("#000");

      chart.legend.maxHeight = 210;
      chart.legend.scrollable = true;
      chart.legend.position = "right";
      chart.logo.disabled = true;
      chart.padding(0, 0, 0, 0);
      chart.radius = am4core.percent(95);
      chart.paddingRight = 0;
      var userId:any = this.authService.getuserId();
      var fromDate:any = localStorage.getItem('dbFromDate');
      var toDate:any = localStorage.getItem('dbToDate');
      let filterData = {
        'userId': userId,
        'fromDate': fromDate,
        'toDate': toDate
      }
      this.orgadmin.getUploadDetails(filterData).subscribe((uploadinfo: any)=>{
        this.getuploadinfo=uploadinfo.data.candidateStatusCountDto;
        //console.log(this.getuploadinfo);
        let data = [];
        for (let i = 0; i < this.getuploadinfo.length; i++) {
          // let obj={};
          // obj=this.getuploadinfo[i].statusName;
          data.push({name: this.getuploadinfo[i].statusName, value: this.getuploadinfo[i].count, statcode: this.getuploadinfo[i].statusCode });

        }
        chart.data = data;

      });
// Add and configure Series
let pieSeries = chart.series.push(new am4charts.PieSeries());

pieSeries.slices.template.tooltipText = "{category}: {value}";
pieSeries.labels.template.disabled = true;
pieSeries.dataFields.value = "value";
pieSeries.dataFields.category = "name";
pieSeries.slices.template.stroke = am4core.color("#fff");
pieSeries.slices.template.strokeWidth = 2;
pieSeries.slices.template.strokeOpacity = 1;
// This creates initial animation
pieSeries.hiddenState.properties.opacity = 1;
pieSeries.hiddenState.properties.endAngle = -90;
pieSeries.hiddenState.properties.startAngle = -90;
pieSeries.legendSettings.itemValueText = "[bold]{value}[/bold]";
pieSeries.colors.list = [
  am4core.color("#FF8E00"),
  am4core.color("#ffd400"),
  am4core.color("#fd352c"),
  am4core.color("#08e702"),
  am4core.color("#9c27b0"),
  am4core.color("#021aee"),
];

// var rgm = new am4core.RadialGradientModifier();
// rgm.brightnesses.push(-0.3, -0.3, -0.1, 0, - 0.1);
// pieSeries.slices.template.fillModifier = rgm;
// pieSeries.slices.template.strokeModifier = rgm;
// pieSeries.slices.template.strokeWidth = 0;

//pieSeries.slices.template.events.on("hit", myFunction, this);
pieSeries.slices.template.events.on('hit', (e) => {
  const getchartData = e.target._dataItem as any;
  const statuscodes = getchartData._dataContext.statcode;
  //console.log(statuscodes);
  this.orgadmin.setStatusCode(statuscodes);
  window.location.reload();
});
chart.legend.itemContainers.template.events.on("hit", (ev) => {
  const getchartData = ev.target._dataItem as any;
  const statuscodes = getchartData._label._dataItem._dataContext.statcode;
  this.orgadmin.setStatusCode(statuscodes);
  window.location.reload();
});
pieSeries.slices.template.cursorOverStyle = am4core.MouseCursorStyle.pointer;
    });

}
  ngOnDestroy() {
    this.zone.runOutsideAngular(() => {
      if (this.chart) {
        this.chart.dispose();
      }
    });
  }

  sendinvitation(){
    this.patchUserValues();
    return this.orgadmin.saveInvitationSent(this.formSendInvitation.value).subscribe((result:any)=>{
      if(result.outcome === true){
        Swal.fire({
          title: result.message,
          icon: 'success'
        }).then((result) => {
          if (result.isConfirmed) {
            window.location.reload();
          }
        });
      }else{
        Swal.fire({
          title: result.message,
          icon: 'warning'
        })
      }
  });
  }

  reInvite(){
    this.reInvitePatchValues();
    return this.orgadmin.saveInvitationSent(this.formSendInvitation.value).subscribe((result:any)=>{
      if(result.outcome === true){
        Swal.fire({
          title: result.message,
          icon: 'success'
        }).then((result) => {
          if (result.isConfirmed) {
            window.location.reload();
          }
        });
      }else{
        Swal.fire({
          title: result.message,
          icon: 'warning'
        })
      }
  });
  }


  activeInactive(referenceNo:any){
    return this.orgadmin.putAgentStat(referenceNo).subscribe((result:any)=>{
      if(result.outcome === true){
        Swal.fire({
          title: result.message,
          icon: 'success'
        }).then((result) => {
          if (result.isConfirmed) {
            window.location.reload();
          }
        });
      }else{
        Swal.fire({
          title: result.message,
          icon: 'warning'
        })
      }
  });
  }

  childCheck(e:any){
    var sid = e.target.id;
    if (e.target.checked) {
      this.tmp.push(sid);
    } else {
      this.tmp.splice($.inArray(sid, this.tmp),1);
    }
  }
  childCheckselected(sid:any){
    this.tmp.push(sid);
  }
  selectAll(e:any){
    if (e.target.checked) {
      $(".childCheck").prop('checked', true);
      var  cboxRolesinput = $('.childCheck');
      var arrNumber:any = [];
      $.each(cboxRolesinput,function(idx,elem){
       // var inputValues:any  = $(elem).val();
        // console.log(inputValues);
        arrNumber.push($(this).val());
      });

      this.tmp = arrNumber;
      console.log(this.tmp);
    } else {
      $(".childCheck").prop('checked', false);
    }

  }
//*****************UPDATE CANDIDATE*****************//
  openModal(modalData:any, userId:any){
     this.modalService.open(modalData, {
      centered: true,
      backdrop: 'static'
     });
     this.orgadmin.getCandidateDetails(userId).subscribe((result: any)=>{
      this.getCandidate=result.data;
      const contactNumber = this.getCandidate.contactNumber;
      const emailId = this.getCandidate.emailId;
      this.showContactNumberError = contactNumber && contactNumber.length < 10;
      const emailPattern = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
      const isEmailValid = emailPattern.test(emailId);
      this.showEmailIdError = !isEmailValid;
      this.updateCandidate.patchValue({
        candidateName: this.getCandidate.candidateName,
        applicantId: this.getCandidate.applicantId,
        createdByUserFirstName: this.getCandidate.createdByUserFirstName,
        candidateCode: this.getCandidate.candidateCode,
        contactNumber: this.getCandidate.contactNumber,
        emailId: this.getCandidate.emailId
       });
    });
  }
  onSubmit(updateCandidate:FormGroup) {
    if(this.updateCandidate.valid){
     this.orgadmin.putCandidateData(this.updateCandidate.value).subscribe((result:any)=>{
        if(result.outcome === true){
          Swal.fire({
            title: result.message,
            icon: 'success'
          }).then((result) => {
            if (result.isConfirmed) {
              window.location.reload();
            }
          });
        }else{
          Swal.fire({
            title: result.message,
            icon: 'warning'
          })
        }
  });
  }else{
    const invalidFields = Object.keys(updateCandidate.controls).filter(key => updateCandidate.get(key)?.invalid);
  console.warn("gsh",invalidFields)
      const errorMessage = `Invalid Fields: ${invalidFields.join(', ')}`;
    Swal.fire({
      title: errorMessage,
      icon: 'warning'
    })
  }
  }
  ChartDataListingpagination(): any[] {
    const filteredItems = this.ChartDataListing.filter((item: any) => this.searchFilter(item));
    const startIndex = this.currentPageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return filteredItems.slice(startIndex, endIndex);
    }

  goToNextPage(): void {
    if (this.currentPageIndex < this.totalPages - 1) {
    this.currentPageIndex++;
    }
//adding below lines to call the next page records
    if(this.getStatCodes){
      var userId:any = this.authService.getuserId();
      var fromDate:any = localStorage.getItem('dbFromDate');
      var toDate:any = localStorage.getItem('dbToDate');
      let filterData = {
        'userId': userId,
        'fromDate': fromDate,
        'toDate': toDate,
        'status': this.getStatCodes,
        //adding below parameter to get the backend pagination list
        'pageNumber':this.currentPageIndex
      }
      this.orgadmin.getChartDetails(filterData).subscribe((data: any)=>{
        this.ChartDataListing=data.data.candidateDtoList;
        // console.log(this.ChartDataListing);
        //this.ChartDataListing.reverse();
        console.log("After : ", this.ChartDataListing)
        //console.log(data);
        const startIndex = this.currentPageIndex * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        return this.ChartDataListing.slice(startIndex, endIndex);
      });

    }
  }

  goToPrevPage(): void {
    // this.idvalue=idvalue;
    if (this.currentPageIndex > 0) {
    this.currentPageIndex--;
    }

    //adding below lines to call the previous page records
    if(this.getStatCodes){
      var userId:any = this.authService.getuserId();
      var fromDate:any = localStorage.getItem('dbFromDate');
      var toDate:any = localStorage.getItem('dbToDate');
      let filterData = {
        'userId': userId,
        'fromDate': fromDate,
        'toDate': toDate,
        'status': this.getStatCodes,
        //adding below parameter to get the backend pagination list
        'pageNumber':this.currentPageIndex
      }
      this.orgadmin.getChartDetails(filterData).subscribe((data: any)=>{
        this.ChartDataListing=data.data.candidateDtoList;
        // console.log(this.ChartDataListing);
        //this.ChartDataListing.reverse();
        console.log("After : ", this.ChartDataListing)
        //console.log(data);
        const startIndex = this.currentPageIndex * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        return this.ChartDataListing.slice(startIndex, endIndex);
      });

    }
  }

  // get totalPages(): number {
  //   const filteredItems = this.ChartDataListing.filter((item: any) => this.searchFilter(item));
  //   return Math.ceil(filteredItems.length / this.pageSize);
  //   }

  get totalPages(): number {
    for (let i = 0; i < this.getuploadinfo.length; i++) {
      if(this.getuploadinfo[i].statusCode==this.getStatCodes){
        console.log("Total PAges::{}",Math.ceil(this.getuploadinfo[i].count / this.pageSize));
        return Math.ceil(this.getuploadinfo[i].count / this.pageSize);
      }
    }
    return 0;
  }

    searchFilter(item: any): boolean {
      const searchText = this.searchText.toLowerCase();
      const candidateName = item.candidateName.toLowerCase();
      const emailId = item.emailId.toLowerCase();
      const contactNumber = item.contactNumber.toLowerCase();
      const applicantId = item.applicantId.toLowerCase();
      const candidateStatusName = item.candidateStatusName.toLowerCase();

      return candidateName.includes(searchText.toLowerCase()) ||
             emailId.includes(searchText.toLowerCase()) ||
             contactNumber.includes(searchText.toLowerCase()) ||
             applicantId.includes(searchText.toLowerCase()) ||
             candidateStatusName.includes(searchText.toLocaleLowerCase());
  }

}


