package com.bjpowernode.crm.settings.web.controller;

import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.settings.base.Settings;
import com.bjpowernode.crm.settings.domain.DictionaryType;
import com.bjpowernode.crm.settings.service.DictionaryService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/settings/dictionary")
public class DictionaryController extends Settings {

    @Autowired
    private DictionaryService dictionaryService;

    /*
    跳转到字典模块首页面
     */
    @RequestMapping("/toIndex.do")
    public String toIndex() {
        return "/settings/dictionary/index";
    }

    /*
    跳转到字典类型模块首页面
     */
    @RequestMapping("/type/toIndex.do")
    public String toTypeIndex(Model model) {

        //查询出字典类型列表数据
        List<DictionaryType> dictionaryTypeList = dictionaryService.findDictionaryTypeList();

        //校验
        if (!CollectionUtils.isEmpty(dictionaryTypeList))
            //存入到model中,携带到页面
            model.addAttribute("dictionaryTypeList", dictionaryTypeList);

        return "/settings/dictionary/type/index";
    }


    @RequestMapping("/type/toSave.do")
    public String toTypeSave() {
        return "/settings/dictionary/type/save";
    }

    /*
    跳转到字典值模块首页面
     */
    @RequestMapping("/value/toIndex.do")
    public String toValueIndex() {
        return "/settings/dictionary/value/index";
    }


    @RequestMapping("/type/checkCode.do")
    @ResponseBody
    public R checkCode(@RequestParam("code") String code) {
        //可以查询数据库,查询编码是否存在
        //可以通过编码查询字典类型的数量
        //select count(id) from tbl_dic_type where code = #{code} -> 返回值是int类型
        //也可以通过编码查询字典类型对象
        //select * from tbl_dic_type where code = #{code} -> 返回值是对象
        DictionaryType dictionaryType = dictionaryService.findDictionaryType(code);

        //数据库没有查询到当前的字典类型对象,证明当前的编码是可以新增的
        if (ObjectUtils.isEmpty(dictionaryType))
            return R.builder()
                    .code(State.SUCCESS.getCode())
                    .msg(State.SUCCESS.getMsg())
                    .success(true)
                    .build();
        else
            //数据库查询到了当前的编码对应的字典类型对象,不能够新增
            return R.builder()
                    .code(State.DB_FIND_EXISTS_ERROR.getCode())
                    .msg(State.DB_FIND_EXISTS_ERROR.getMsg())
                    .success(false)
                    .build();
    }


    @RequestMapping("/type/saveDictionaryType.do")
    @ResponseBody
    //public R saveDictionaryType(@RequestBody Map<String,String> dictionaryType){
    public R saveDictionaryType(@RequestBody DictionaryType dictionaryType) {
        //校验参数信息
        if (StringUtils.isBlank(dictionaryType.getCode()))
            throw new RuntimeException(State.PARAMS_ERROR.getMsg());

        //校验通过,新增字典类型
        //在配置文件中通过Spring声明式事务控制来进行事务的操作
        //service的命名方法,必须以save update delete开头
        boolean flag = dictionaryService.saveDictionaryType(dictionaryType);

        return flag
                ?
                R.builder()
                        .code(State.SUCCESS.getCode())
                        .msg(State.SUCCESS.getMsg())
                        .success(true)
                        .build()
                :
                R.builder()
                        .code(State.DB_SAVE_ERROR.getCode())
                        .msg(State.DB_SAVE_ERROR.getMsg())
                        .success(false)
                        .build();
    }


    /*
        根据code查询字典类型数据,并跳转到修改页面操作
     */
    @RequestMapping("/type/toEdit.do")
    public String toTypeEdit(@RequestParam("code") String code, Model model) {
        //通过code查询字典类型对象
        DictionaryType dictionaryType = dictionaryService.findDictionaryType(code);

        //如果字典类型对象不为空,存入到Model对象中,携带到页面进行加载
        if (ObjectUtils.isNotEmpty(dictionaryType))
            model.addAttribute("dictionaryType", dictionaryType);

        //跳转到字典类型的修改页面
        return "/settings/dictionary/type/edit";
    }

    /*
        修改字典类型操作
            一定要记住接收参数的注解的使用
                @RequestBody
                @RequestParam
     */
    @RequestMapping("/type/updateDictionaryType.do")
    @ResponseBody
    public R updateDictionaryType(@RequestBody DictionaryType dictionaryType) {
        //校验参数的合法性
        checked(
                //只校验必传的参数,而不是校验所有参数信息
                dictionaryType.getCode()
        );

        //更新操作
        boolean flag = dictionaryService.updateDictionaryType(dictionaryType);

        return flag ? ok() : err(State.DB_UPDATE_ERROR);
    }


    @RequestMapping("/type/batchDelete.do")
    @ResponseBody
    public R batchDeleteDictionaryType(@RequestBody List<String> codes) {
        /*
            批量删除操作,要考虑表与表之间的关系
                tbl_dic_type 和 tbl_dic_value表之间的关系(一对多的关系)
                现在我们要删除的是一方数据,如果当前删除的一方数据,有关联的多方数据,不能删除
                如果想要删除这个一方数据,必须要先将所有的多方数据删除后,再删除一方数据
                如果有了外键的约束,由mysql帮助我们自动维护,这样我们不用担心这个问题
            但是现在公司中,由于使用外键,会让mysql分出一部分性能来维护外键关系
            那么我们公司中,可以不使用外键进行维护,我们通过逻辑进行维护
            在删除一方数据的时候,需要关联查询一下这条数据对应的多方数据
            如果没有多方数据的关联,那么则可以直接删除
         */
        //返回值是无法删除的多方数据
        List<String> relationCodeList = dictionaryService.deleteDictionaryList(codes);

        //将无法删除的code编码返回,给前端用户进行展示,告知
        return CollectionUtils.isEmpty(relationCodeList) ?
                //全部数据已经被删除
                ok() :
                //可能全部失败,或部分失败
                ok(State.DB_DELETE_ERROR, relationCodeList, false);
    }
}
